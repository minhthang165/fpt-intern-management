package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.ScheduleDTO;
import com.fsoft.fintern.dtos.SchedulingDTO;
import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.models.Schedule;
import com.fsoft.fintern.models.Subject;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.RoomRepository;
import com.fsoft.fintern.repositories.ScheduleRepository;
import com.fsoft.fintern.repositories.SubjectRepository;
import com.fsoft.fintern.repositories.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class SchedulingService {

    private final ExcelTemplateGenerator excelTemplateGenerator;
    private final RoomRepository roomRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;


    private Map<Integer, MentorInfo> mentorMap = new HashMap<>();


    private static final String CODE_SUBJECT = "Code";
    private static final String JAPANESE_SUBJECT = "Japanese";
    private static final String KOREAN_SUBJECT = "Korean";


    private static final LocalTime MORNING_START = LocalTime.of(7, 30);
    private static final LocalTime MORNING_END = LocalTime.of(11, 30);
    private static final LocalTime LANGUAGE_START = LocalTime.of(7, 30);
    private static final LocalTime LANGUAGE_END = LocalTime.of(9, 0);
    private static final LocalTime NOON_START = LocalTime.of(9, 0);
    private static final LocalTime NOON_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);
    private static final LocalTime AFTERNOON_START_LANGUAGE = LocalTime.of(14, 00);


    private static final LocalDate DEFAULT_START_DATE = LocalDate.now();
    private static final LocalDate DEFAULT_END_DATE = DEFAULT_START_DATE.plusMonths(3);

    @Autowired
    public SchedulingService(ExcelTemplateGenerator excelTemplateGenerator,
                             RoomRepository roomRepository,
                             SubjectRepository subjectRepository,
                             ClassroomRepository classroomRepository,
                             ScheduleRepository scheduleRepository,
                             UserRepository userRepository) {
        this.excelTemplateGenerator = excelTemplateGenerator;
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }


    public byte[] generateTemplate() throws IOException {
        return excelTemplateGenerator.generateSchedulingTemplate();
    }


    public List<SchedulingDTO> importSchedulingData(MultipartFile file) throws IOException {
        List<SchedulingDTO> results = new ArrayList<>();

        mentorMap.clear();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // First, read mentor information from Mentors sheet
            Sheet mentorSheet = workbook.getSheet("Mentors");
            if (mentorSheet == null) {
                throw new IOException("Invalid template: 'Mentors' sheet not found");
            }

            // Skip header row
            for (int i = 1; i <= mentorSheet.getLastRowNum(); i++) {
                Row row = mentorSheet.getRow(i);
                if (row == null) continue;

                String mentorIdStr = getCellValueAsString(row.getCell(0));
                String name = getCellValueAsString(row.getCell(1));
                String specialization = getCellValueAsString(row.getCell(2));
                String mentorType = getCellValueAsString(row.getCell(3));
                String maxHoursStr = getCellValueAsString(row.getCell(4));
                String minHoursStr = getCellValueAsString(row.getCell(5));

                // Skip empty rows
                if (mentorIdStr.isEmpty()) continue;

                try {
                    Integer mentorId = Integer.parseInt(mentorIdStr);
                    Integer maxHours = maxHoursStr.isEmpty() ? 40 : Integer.parseInt(maxHoursStr);
                    Integer minHours = minHoursStr.isEmpty() ? 25 : Integer.parseInt(minHoursStr);

                    MentorInfo mentorInfo = new MentorInfo(mentorId, name, specialization, mentorType, maxHours, minHours, 0);
                    mentorMap.put(mentorId, mentorInfo);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number format in Mentors sheet: " + e.getMessage());
                }
            }

            // Then read class information from Classes sheet
            Sheet sheet = workbook.getSheet("Classes");
            if (sheet == null) {
                throw new IOException("Invalid template: 'Classes' sheet not found");
            }

            // Skip the header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String classId = getCellValueAsString(row.getCell(0));
                String className = getCellValueAsString(row.getCell(1));
                String classTypeStr = getCellValueAsString(row.getCell(2));
                String languageTypeStr = getCellValueAsString(row.getCell(3));
                String roomIdStr = getCellValueAsString(row.getCell(4));
                String codeMentorIdStr = getCellValueAsString(row.getCell(5));
                String languageMentorIdStr = getCellValueAsString(row.getCell(6));

                // Skip empty rows
                if (classId.isEmpty() && className.isEmpty()) continue;

                ClassType classType = null;
                LanguageType languageType = null;
                Integer roomId = null;
                Integer codeMentorId = null;
                Integer languageMentorId = null;

                // Validate and parse class type
                if (!classTypeStr.isEmpty()) {
                    try {
                        classType = ClassType.valueOf(classTypeStr);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("Invalid class type: " + classTypeStr);
                    }
                } else {
                    throw new IOException("Class type is required for class: " + className);
                }

                // Handle language type based on class type
                if (!languageTypeStr.isEmpty()) {
                    try {
                        languageType = LanguageType.valueOf(languageTypeStr);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("Invalid language type: " + languageTypeStr);
                    }
                } else {
                    // languageType can be null only for CODE_ONLY classes
                    if (classType != ClassType.CODE_ONLY) {
                        throw new IOException("Language type is required for " + classType + " classes. Class: " + className);
                    }
                }

                // Parse room ID if provided
                if (!roomIdStr.isEmpty()) {
                    try {
                        roomId = Integer.parseInt(roomIdStr);
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid room ID: " + roomIdStr);
                    }
                }

                // Parse code mentor ID if provided
                if (!codeMentorIdStr.isEmpty()) {
                    try {
                        codeMentorId = Integer.parseInt(codeMentorIdStr);
                        // Validate code mentor exists and is a CodeMentor
                        MentorInfo mentorInfo = mentorMap.get(codeMentorId);
                        if (mentorInfo == null) {
                            throw new IOException("Code mentor with ID " + codeMentorId + " not found in Mentors sheet");
                        }
                        if (!mentorInfo.type.equals("CodeMentor")) {
                            throw new IOException("Mentor with ID " + codeMentorId + " is not a CodeMentor");
                        }
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid code mentor ID: " + codeMentorIdStr);
                    }
                }

                // Parse language mentor ID if provided
                if (!languageMentorIdStr.isEmpty()) {
                    try {
                        languageMentorId = Integer.parseInt(languageMentorIdStr);
                        // Validate language mentor exists and is a LanguageMentor
                        MentorInfo mentorInfo = mentorMap.get(languageMentorId);
                        if (mentorInfo == null) {
                            throw new IOException("Language mentor with ID " + languageMentorId + " not found in Mentors sheet");
                        }
                        if (!mentorInfo.type.equals("LanguageMentor")) {
                            throw new IOException("Mentor with ID " + languageMentorId + " is not a LanguageMentor");
                        }
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid language mentor ID: " + languageMentorIdStr);
                    }
                }

                // Validate mentor assignments based on class type
                if (classType == ClassType.CODE_ONLY) {
                    if (codeMentorId == null) {
                        throw new IOException("Code mentor is required for CODE_ONLY class: " + className);
                    }
                    if (languageMentorId != null) {
                        throw new IOException("Language mentor should not be assigned for CODE_ONLY class: " + className);
                    }
                } else if (classType == ClassType.LANGUAGE_ONLY) {
                    if (languageMentorId == null) {
                        throw new IOException("Language mentor is required for LANGUAGE_ONLY class: " + className);
                    }
                    if (codeMentorId != null) {
                        throw new IOException("Code mentor should not be assigned for LANGUAGE_ONLY class: " + className);
                    }
                } else if (classType == ClassType.COMBINED) {
                    if (codeMentorId == null || languageMentorId == null) {
                        throw new IOException("Both code mentor and language mentor are required for COMBINED class: " + className);
                    }
                }

                SchedulingDTO dto = new SchedulingDTO(classId, className, classType, languageType, roomId, codeMentorId, languageMentorId);
                results.add(dto);
            }
        }

        return results;
    }


    public List<ScheduleDTO> generateSchedule(List<SchedulingDTO> data) {
        List<ScheduleDTO> result = new ArrayList<>();

        // 1. Lấy danh sách phòng học từ database
        List<Room> availableRooms = roomRepository.findAll();
        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No rooms available in database");
        }

        // Lưu trữ số lần sử dụng của mỗi phòng để ưu tiên sử dụng ít phòng
        Map<Integer, Integer> roomUsageCount = new HashMap<>();
        for (Room room : availableRooms) {
            roomUsageCount.put(room.getId(), 0);
        }

        // 2. Lấy hoặc tạo mới các subject nếu chưa có
        Subject codeSubject = getOrCreateSubject(CODE_SUBJECT);
        Subject japaneseSubject = getOrCreateSubject(JAPANESE_SUBJECT);
        Subject koreanSubject = getOrCreateSubject(KOREAN_SUBJECT);

        // 3. Sắp xếp các lớp học theo độ ưu tiên
        // Độ ưu tiên: COMBINED > LANGUAGE_ONLY > CODE_ONLY
        Collections.sort(data, (a, b) -> {
            int priorityA = getPriority(a.getClassType());
            int priorityB = getPriority(b.getClassType());
            return Integer.compare(priorityB, priorityA); // Giảm dần
        });

        // 4. Tạo cấu trúc dữ liệu để theo dõi việc sử dụng phòng học
        // Map<DayOfWeek, Map<Room, Set<TimeSlot>>>
        Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule = initializeRoomSchedule();

        // 5. Lặp qua từng lớp học để xếp lịch
        for (SchedulingDTO classData : data) {
            List<ScheduleDTO> classSchedules = scheduleClass(classData, availableRooms, roomSchedule,
                    codeSubject, japaneseSubject, koreanSubject, roomUsageCount);

            result.addAll(classSchedules);
        }

        return result;
    }

    private int getPriority(ClassType classType) {
        switch (classType) {
            case COMBINED:
                return 3;
            case LANGUAGE_ONLY:
                return 2;
            case CODE_ONLY:
                return 1;
            default:
                return 0;
        }
    }

    private Map<String, Map<Integer, Set<TimeSlot>>> initializeRoomSchedule() {
        Map<String, Map<Integer, Set<TimeSlot>>> schedule = new HashMap<>();

        // Khởi tạo lịch cho mỗi ngày trong tuần
        for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
            schedule.put(day, new HashMap<>());
        }

        return schedule;
    }

    private List<ScheduleDTO> scheduleClass(SchedulingDTO classData, List<Room> availableRooms,
                                            Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                            Subject codeSubject, Subject japaneseSubject, Subject koreanSubject,
                                            Map<Integer, Integer> roomUsageCount) {
        List<ScheduleDTO> classSchedules = new ArrayList<>();

        switch (classData.getClassType()) {
            case LANGUAGE_ONLY:
                // Đảm bảo languageType không null cho lớp LANGUAGE_ONLY
                if (classData.getLanguageType() == null) {
                    throw new RuntimeException("Language type cannot be null for LANGUAGE_ONLY class: " + classData.getClassName());
                }

                scheduleLanguageOnlyClass(classData, availableRooms, roomSchedule,
                        classData.getLanguageType() == LanguageType.JAPANESE ? japaneseSubject : koreanSubject,
                        classSchedules, roomUsageCount);
                break;

            case CODE_ONLY:
                // Với lớp CODE_ONLY, languageType có thể null
                scheduleCodeOnlyClass(classData, availableRooms, roomSchedule, codeSubject, classSchedules, roomUsageCount);
                break;

            case COMBINED:
                // Đảm bảo languageType không null cho lớp COMBINED
                if (classData.getLanguageType() == null) {
                    throw new RuntimeException("Language type cannot be null for COMBINED class: " + classData.getClassName());
                }

                scheduleCombinedClass(classData, availableRooms, roomSchedule, codeSubject,
                        classData.getLanguageType() == LanguageType.JAPANESE ? japaneseSubject : koreanSubject,
                        classSchedules, roomUsageCount);
                break;
        }

        return classSchedules;
    }

    private void scheduleLanguageOnlyClass(SchedulingDTO classData, List<Room> availableRooms,
                                           Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                           Subject languageSubject, List<ScheduleDTO> classSchedules,
                                           Map<Integer, Integer> roomUsageCount) {
        // Language-only classes fixed at 7:30-9:00

        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                    .filter(r -> r.getId().equals(classData.getRoomId()))
                    .findFirst()
                    .orElse(null);
        }

        // Nếu không có phòng được chọn, tìm phòng khả dụng theo độ ưu tiên sử dụng
        if (selectedRoom == null) {
            // Tạo danh sách các phòng sắp xếp theo số lần sử dụng (nhiều nhất lên đầu)
            List<Room> sortedRooms = new ArrayList<>(availableRooms);
            sortedRooms.sort((a, b) -> Integer.compare(
                    roomUsageCount.getOrDefault(b.getId(), 0),
                    roomUsageCount.getOrDefault(a.getId(), 0)
            ));

            for (Room room : sortedRooms) {
                boolean canUse = true;
                // Kiểm tra phòng có sẵn cho tất cả các ngày từ thứ 2 đến thứ 6
                for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                    if (!isRoomAvailable(room, day, LANGUAGE_START, LANGUAGE_END, roomSchedule)) {
                        canUse = false;
                        break;
                    }
                }
                if (canUse) {
                    selectedRoom = room;
                    break;
                }
            }
        }

        if (selectedRoom == null) {
            throw new RuntimeException("No available room for class: " + classData.getClassName() +
                    " that can be used from Monday to Friday");
        }

        // Xếp lịch vào tất cả các ngày từ thứ 2 đến thứ 6
        for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
            // Đánh dấu phòng đã được sử dụng
            markRoomAsOccupied(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule);

            // Tăng số lần sử dụng phòng
            roomUsageCount.put(selectedRoom.getId(), roomUsageCount.getOrDefault(selectedRoom.getId(), 0) + 1);

            // Tạo lịch học với languageMentorId
            ScheduleDTO scheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    languageSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    LANGUAGE_START,
                    LANGUAGE_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE,
                    classData.getLanguageMentorId() // Sử dụng languageMentorId cho LANGUAGE_ONLY
            );

            classSchedules.add(scheduleDTO);
        }
    }

    private void scheduleCodeOnlyClass(SchedulingDTO classData, List<Room> availableRooms,
                                       Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                       Subject codeSubject, List<ScheduleDTO> classSchedules,
                                       Map<Integer, Integer> roomUsageCount) {
        // Code-only classes flexible: morning (7:30-11:30) or afternoon (13:00-17:00)

        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                    .filter(r -> r.getId().equals(classData.getRoomId()))
                    .findFirst()
                    .orElse(null);
        }

        // Nếu không có phòng được chọn, tìm phòng khả dụng theo độ ưu tiên sử dụng
        if (selectedRoom == null) {
            // Tạo danh sách các phòng sắp xếp theo số lần sử dụng (nhiều nhất lên đầu)
            List<Room> sortedRooms = new ArrayList<>(availableRooms);
            sortedRooms.sort((a, b) -> Integer.compare(
                    roomUsageCount.getOrDefault(b.getId(), 0),
                    roomUsageCount.getOrDefault(a.getId(), 0)
            ));

            for (Room room : sortedRooms) {
                boolean canUse = true;
                // Kiểm tra phòng có sẵn cho tất cả các ngày từ thứ 2 đến thứ 6 (buổi sáng)
                for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                    if (!isRoomAvailable(room, day, MORNING_START, MORNING_END, roomSchedule)) {
                        canUse = false;
                        break;
                    }
                }

                if (canUse) {
                    selectedRoom = room;
                    break;
                }

                // Nếu không thể xếp buổi sáng, thử buổi chiều
                canUse = true;
                for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                    if (!isRoomAvailable(room, day, AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                        canUse = false;
                        break;
                    }
                }

                if (canUse) {
                    selectedRoom = room;
                    break;
                }
            }
        }

        if (selectedRoom == null) {
            throw new RuntimeException("No available room for class: " + classData.getClassName() +
                    " that can be used from Monday to Friday");
        }

        // Kiểm tra có thể xếp lịch buổi sáng cho tất cả các ngày không
        boolean canScheduleMorning = true;
        for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
            if (!isRoomAvailable(selectedRoom, day, MORNING_START, MORNING_END, roomSchedule)) {
                canScheduleMorning = false;
                break;
            }
        }

        // Nếu có thể xếp buổi sáng, thì xếp tất cả các ngày buổi sáng
        if (canScheduleMorning) {
            for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                // Đánh dấu phòng đã được sử dụng
                markRoomAsOccupied(selectedRoom, day, MORNING_START, MORNING_END, roomSchedule);

                // Tăng số lần sử dụng phòng
                roomUsageCount.put(selectedRoom.getId(), roomUsageCount.getOrDefault(selectedRoom.getId(), 0) + 1);

                // Tạo lịch học với codeMentorId
                ScheduleDTO scheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        MORNING_START,
                        MORNING_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getCodeMentorId() // Sử dụng codeMentorId cho CODE_ONLY
                );

                classSchedules.add(scheduleDTO);
            }
        } else {
            // Nếu không thể xếp buổi sáng, xếp buổi chiều
            for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                // Đánh dấu phòng đã được sử dụng
                markRoomAsOccupied(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);

                // Tăng số lần sử dụng phòng
                roomUsageCount.put(selectedRoom.getId(), roomUsageCount.getOrDefault(selectedRoom.getId(), 0) + 1);

                // Tạo lịch học với codeMentorId
                ScheduleDTO scheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        AFTERNOON_START,
                        AFTERNOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getCodeMentorId() // Sử dụng codeMentorId cho CODE_ONLY
                );

                classSchedules.add(scheduleDTO);
            }
        }
    }

    private void scheduleCombinedClass(SchedulingDTO classData, List<Room> availableRooms,
                                       Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                       Subject codeSubject, Subject languageSubject,
                                       List<ScheduleDTO> classSchedules,
                                       Map<Integer, Integer> roomUsageCount) {
        // Combined classes with flexible morning/afternoon scheduling

        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        boolean codeInMorning = true; // Mặc định code học buổi sáng

        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                    .filter(r -> r.getId().equals(classData.getRoomId()))
                    .findFirst()
                    .orElse(null);
        }

        // Nếu không có phòng được chọn, tìm phòng khả dụng theo độ ưu tiên sử dụng
        if (selectedRoom == null) {
            // Tạo danh sách các phòng sắp xếp theo số lần sử dụng (nhiều nhất lên đầu)
            List<Room> sortedRooms = new ArrayList<>(availableRooms);
            sortedRooms.sort((a, b) -> Integer.compare(
                    roomUsageCount.getOrDefault(b.getId(), 0),
                    roomUsageCount.getOrDefault(a.getId(), 0)
            ));

            // Thử tìm phòng với code học buổi sáng trước
            for (Room room : sortedRooms) {
                boolean canUse = true;
                for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                    if (!isRoomAvailable(room, day, MORNING_START, MORNING_END, roomSchedule) ||
                            !isRoomAvailable(room, day, AFTERNOON_START_LANGUAGE, AFTERNOON_END, roomSchedule)) {
                        canUse = false;
                        break;
                    }
                }

                if (canUse) {
                    selectedRoom = room;
                    codeInMorning = true;
                    break;
                }
            }

            // Nếu không tìm được phòng với code học buổi sáng, thử tìm với code học buổi chiều
            if (selectedRoom == null) {
                for (Room room : sortedRooms) {
                    boolean canUse = true;
                    for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
                        if (!isRoomAvailable(room, day, NOON_START, NOON_END, roomSchedule) ||
                                !isRoomAvailable(room, day, AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                            canUse = false;
                            break;
                        }
                    }

                    if (canUse) {
                        selectedRoom = room;
                        codeInMorning = false;
                        break;
                    }
                }
            }
        }

        if (selectedRoom == null) {
            throw new RuntimeException("No available room for combined class: " + classData.getClassName() +
                    " that can be used from Monday to Friday for both morning and afternoon");
        }

        // Xếp lịch cho tất cả các ngày từ thứ 2 đến thứ 6
        for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")) {
            if (codeInMorning) {
                // Sáng học lập trình
                markRoomAsOccupied(selectedRoom, day, MORNING_START, MORNING_END, roomSchedule);
                ScheduleDTO codeScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        MORNING_START,
                        MORNING_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getCodeMentorId()
                );
                classSchedules.add(codeScheduleDTO);

                // Chiều học ngoại ngữ (14:00-17:00)
                markRoomAsOccupied(selectedRoom, day, AFTERNOON_START_LANGUAGE, AFTERNOON_END, roomSchedule);
                ScheduleDTO langScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        languageSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        AFTERNOON_START_LANGUAGE,
                        AFTERNOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getLanguageMentorId()
                );
                classSchedules.add(langScheduleDTO);
            } else {
                // Sáng học ngoại ngữ (9:00-12:00)
                markRoomAsOccupied(selectedRoom, day, NOON_START, NOON_END, roomSchedule);
                ScheduleDTO langScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        languageSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        NOON_START,
                        NOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getLanguageMentorId()
                );
                classSchedules.add(langScheduleDTO);

                // Chiều học lập trình
                markRoomAsOccupied(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);
                ScheduleDTO codeScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        AFTERNOON_START,
                        AFTERNOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE,
                        classData.getCodeMentorId()
                );
                classSchedules.add(codeScheduleDTO);
            }

            // Tăng số lần sử dụng phòng (2 lần mỗi ngày)
            roomUsageCount.put(selectedRoom.getId(), roomUsageCount.getOrDefault(selectedRoom.getId(), 0) + 2);
        }
    }

    private Subject getOrCreateSubject(String subjectName) {
        Optional<Subject> existingSubject = subjectRepository.findBySubjectName(subjectName);
        if (existingSubject.isPresent()) {
            return existingSubject.get();
        } else {
            Subject newSubject = new Subject();
            newSubject.setSubjectName(subjectName);
            newSubject.setDescription(subjectName);
            return subjectRepository.save(newSubject);
        }
    }

    private boolean isRoomAvailable(Room room, String dayOfWeek, LocalTime startTime, LocalTime endTime,
                                    Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule) {
        Map<Integer, Set<TimeSlot>> daySchedule = roomSchedule.get(dayOfWeek);

        // Nếu phòng chưa được sử dụng trong ngày này
        if (!daySchedule.containsKey(room.getId())) {
            daySchedule.put(room.getId(), new HashSet<>());
            return true;
        }

        // Kiểm tra xem phòng có bị trùng giờ không
        Set<TimeSlot> timeSlots = daySchedule.get(room.getId());
        for (TimeSlot timeSlot : timeSlots) {
            if (timeSlot.overlaps(startTime, endTime)) {
                return false;
            }
        }

        return true;
    }

    private void markRoomAsOccupied(Room room, String dayOfWeek, LocalTime startTime, LocalTime endTime,
                                    Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule) {
        Map<Integer, Set<TimeSlot>> daySchedule = roomSchedule.get(dayOfWeek);

        if (!daySchedule.containsKey(room.getId())) {
            daySchedule.put(room.getId(), new HashSet<>());
        }

        Set<TimeSlot> timeSlots = daySchedule.get(room.getId());
        timeSlots.add(new TimeSlot(startTime, endTime));
    }

    private ScheduleDTO createScheduleDTO(String className, String subjectName, String roomName,
                                          String dayOfWeek, LocalTime startTime, LocalTime endTime,
                                          LocalDate startDate, LocalDate endDate, Integer assignedMentorId) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setClassName(className);
        dto.setSubjectName(subjectName);
        dto.setRoomName(roomName);
        dto.setDayOfWeek(dayOfWeek);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        // Use the assigned mentor ID
        dto.setMentorId(assignedMentorId);

        return dto;
    }

    // Helper method to calculate hours between two times
    private int calculateHours(LocalTime startTime, LocalTime endTime) {
        int startHour = startTime.getHour();
        int endHour = endTime.getHour();
        return endHour - startHour;
    }

    // Helper method to find and assign a mentor with specific specialization and type
    private Integer findAndAssignMentor(Map<Integer, MentorInfo> mentorMap, String specialization, String type, int hours) {
        // Filter mentors by specialization and type
        List<MentorInfo> eligibleMentors = mentorMap.values().stream()
                .filter(m -> specialization.equals(m.specialization) && type.equals(m.type))
                .sorted(Comparator.comparingInt(m -> m.currentHours))
                .toList();

        if (!eligibleMentors.isEmpty()) {
            // Get the mentor with the least current hours
            MentorInfo selectedMentor = eligibleMentors.get(0);
            // Update mentor's hours
            selectedMentor.currentHours += hours;
            return selectedMentor.id;
        }

        return null;
    }

    // Helper method to find any mentor with the lowest current hours
    private Integer findAnyMentorWithLowestHours(Map<Integer, MentorInfo> mentorMap) {
        if (mentorMap.isEmpty()) {
            return null;
        }

        MentorInfo selectedMentor = mentorMap.values().stream()
                .min(Comparator.comparingInt(m -> m.currentHours))
                .orElse(null);

        if (selectedMentor != null) {
            selectedMentor.currentHours += 1; // Increase by 1 hour minimum
            return selectedMentor.id;
        }

        return null;
    }

    public boolean saveSchedule(List<ScheduleDTO> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException("Danh sách lịch học không được để trống");
        }

        int successCount = 0;
        int totalCount = schedules.size();

        for (int i = 0; i < schedules.size(); i++) {
            ScheduleDTO dto = schedules.get(i);

            // Kiểm tra tên lớp
            if (dto.getClassName() == null || dto.getClassName().isEmpty()) {
                throw new IllegalArgumentException("Tên lớp học không được để trống ở lịch thứ " + (i + 1));
            }

            // Kiểm tra tên môn học
            if (dto.getSubjectName() == null || dto.getSubjectName().isEmpty()) {
                throw new IllegalArgumentException("Tên môn học không được để trống ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            // Kiểm tra tên phòng
            if (dto.getRoomName() == null || dto.getRoomName().isEmpty()) {
                throw new IllegalArgumentException("Tên phòng học không được để trống ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            // Lấy thông tin lớp học từ tên lớp
            Optional<Classroom> classroomOpt = classroomRepository.findClassroomByClassName(dto.getClassName());
            if (!classroomOpt.isPresent()) {
                throw new IllegalArgumentException("Không tìm thấy lớp học: " + dto.getClassName());
            }
            Classroom classroom = classroomOpt.get();

            // Lấy thông tin môn học từ tên môn
            Optional<Subject> subjectOpt = subjectRepository.findBySubjectName(dto.getSubjectName());
            if (!subjectOpt.isPresent()) {
                throw new IllegalArgumentException("Không tìm thấy môn học: " + dto.getSubjectName());
            }
            Subject subject = subjectOpt.get();

            // Lấy thông tin phòng học từ tên phòng
            Optional<Room> roomOpt = roomRepository.findByRoomName(dto.getRoomName());
            if (!roomOpt.isPresent()) {
                throw new IllegalArgumentException("Không tìm thấy phòng học: " + dto.getRoomName());
            }
            Room room = roomOpt.get();

            // Kiểm tra thời gian học
            if (dto.getStartTime() == null) {
                throw new IllegalArgumentException("Thời gian bắt đầu không được để trống ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            if (dto.getEndTime() == null) {
                throw new IllegalArgumentException("Thời gian kết thúc không được để trống ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            if (dto.getStartTime().isAfter(dto.getEndTime())) {
                throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            // Kiểm tra ngày học
            if (dto.getDayOfWeek() == null || dto.getDayOfWeek().isEmpty()) {
                throw new IllegalArgumentException("Ngày học không được để trống ở lịch thứ " + (i + 1) + " cho lớp " + dto.getClassName());
            }

            // Tạo đối tượng Schedule và lưu vào database
            Schedule schedule = new Schedule();
            schedule.setClassField(classroom);
            schedule.setSubject(subject);
            schedule.setRoom(room);
            schedule.setDayOfWeek(dto.getDayOfWeek());
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            schedule.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : DEFAULT_START_DATE);
            schedule.setEndDate(dto.getEndDate() != null ? dto.getEndDate() : DEFAULT_END_DATE);

            // Set mentor if mentorId is provided
            String mentorStatus = "Không có mentor";
            if (dto.getMentorId() != null) {
                try {
                    Optional<User> mentorOpt = userRepository.findById(dto.getMentorId());
                    if (mentorOpt.isPresent()) {
                        User mentor = mentorOpt.get();
                        schedule.setMentor(mentor);
                        mentorStatus = "Mentor ID: " + mentor.getId() + ", Tên: " + mentor.getUserName();
                    } else {
                        System.err.println("CẢNH BÁO: Không tìm thấy mentor với ID: " + dto.getMentorId() +
                                " cho lịch học lớp: " + dto.getClassName() +
                                ", môn: " + dto.getSubjectName() +
                                ", ngày: " + dto.getDayOfWeek());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi gán mentor ID " + dto.getMentorId() + ": " + e.getMessage());
                }
            } else {
                System.err.println("CẢNH BÁO: Không có MentorID cho lịch học lớp: " + dto.getClassName() +
                        ", môn: " + dto.getSubjectName() +
                        ", ngày: " + dto.getDayOfWeek());
            }

            try {
                // Lưu schedule vào database
                Schedule savedSchedule = scheduleRepository.save(schedule);
                successCount++;

                System.out.println("Đã lưu lịch học ID: " + savedSchedule.getId() +
                        ", Lớp: " + savedSchedule.getClassField().getClassName() +
                        ", Môn: " + savedSchedule.getSubject().getSubjectName() +
                        ", Phòng: " + savedSchedule.getRoom().getRoomName() +
                        ", Ngày: " + savedSchedule.getDayOfWeek() +
                        ", Giờ: " + savedSchedule.getStartTime() + "-" + savedSchedule.getEndTime() +
                        ", " + mentorStatus);

                if (savedSchedule.getMentor() == null) {
                    System.err.println("CẢNH BÁO: Lịch học ID " + savedSchedule.getId() + " đã được lưu nhưng mentor_id là NULL!");
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi lưu lịch học cho lớp " + dto.getClassName() + ": " + e.getMessage());
            }
        }

        System.out.println("KẾT QUẢ LƯU LỊCH HỌC: " + successCount + "/" + totalCount + " lịch đã được lưu thành công");

        // Xóa thông tin mentor sau khi lưu
        mentorMap.clear();

        return successCount > 0;
    }

    // Helper class để theo dõi thời gian sử dụng phòng học
    private static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;

        public TimeSlot(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
            return !(endTime.isBefore(otherStart) || startTime.isAfter(otherEnd));
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // Helper class to store mentor information
    private static class MentorInfo {
        private Integer id;
        private String name;
        private String specialization;
        private String type;
        private Integer maxHours;
        private Integer minHours;
        private Integer currentHours;

        public MentorInfo(Integer id, String name, String specialization, String type,
                          Integer maxHours, Integer minHours, Integer currentHours) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.type = type;
            this.maxHours = maxHours;
            this.minHours = minHours;
            this.currentHours = currentHours;
        }
    }
}