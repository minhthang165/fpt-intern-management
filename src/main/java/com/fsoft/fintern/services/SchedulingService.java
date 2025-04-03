package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.ScheduleResultDTO;
import com.fsoft.fintern.dtos.SchedulingDTO;
import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.models.Schedule;
import com.fsoft.fintern.models.Subject;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.RoomRepository;
import com.fsoft.fintern.repositories.ScheduleRepository;
import com.fsoft.fintern.repositories.SubjectRepository;
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

    // Các subject mặc định
    private static final String CODE_SUBJECT = "Code";
    private static final String JAPANESE_SUBJECT = "Japanese";
    private static final String KOREAN_SUBJECT = "Korean";

    // Các khung giờ dạy
    private static final LocalTime MORNING_START = LocalTime.of(7, 30);
    private static final LocalTime MORNING_END = LocalTime.of(11, 30);
    private static final LocalTime LANGUAGE_START = LocalTime.of(7, 30);
    private static final LocalTime LANGUAGE_END = LocalTime.of(9, 0);
    private static final LocalTime NOON_START = LocalTime.of(9, 0);
    private static final LocalTime NOON_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);

    // Ngày bắt đầu và kết thúc khóa học
    private static final LocalDate DEFAULT_START_DATE = LocalDate.now();
    private static final LocalDate DEFAULT_END_DATE = DEFAULT_START_DATE.plusMonths(3);

    @Autowired
    public SchedulingService(ExcelTemplateGenerator excelTemplateGenerator,
                             RoomRepository roomRepository,
                             SubjectRepository subjectRepository,
                             ClassroomRepository classroomRepository,
                             ScheduleRepository scheduleRepository) {
        this.excelTemplateGenerator = excelTemplateGenerator;
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
    }


    public byte[] generateTemplate() throws IOException {
        return excelTemplateGenerator.generateSchedulingTemplate();
    }


    public List<SchedulingDTO> importSchedulingData(MultipartFile file) throws IOException {
        List<SchedulingDTO> results = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
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
                
                // Skip empty rows
                if (classId.isEmpty() && className.isEmpty()) continue;
                
                ClassType classType = null;
                LanguageType languageType = null;
                Integer roomId = null;
                
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
                    // For CODE_ONLY classes, languageType remains null
                }
                
                // Parse room ID if provided
                if (!roomIdStr.isEmpty()) {
                    try {
                        roomId = Integer.parseInt(roomIdStr);
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid room ID: " + roomIdStr);
                    }
                }
                
                SchedulingDTO dto = new SchedulingDTO(classId, className, classType, languageType, roomId);
                results.add(dto);
            }
            
            // Đọc thông tin mentor từ sheet Mentors nếu cần
            // Đọc thông tin room từ sheet Rooms nếu cần
            // Đọc các cấu hình từ sheet Config nếu cần
        }
        
        return results;
    }


    public List<ScheduleResultDTO> generateSchedule(List<SchedulingDTO> data) {
        List<ScheduleResultDTO> result = new ArrayList<>();
        
        // 1. Lấy danh sách phòng học từ database
        List<Room> availableRooms = roomRepository.findAll();
        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No rooms available in database");
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
            List<ScheduleResultDTO> classSchedules = scheduleClass(classData, availableRooms, 
                roomSchedule, codeSubject, japaneseSubject, koreanSubject);
            
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
    
    private List<ScheduleResultDTO> scheduleClass(SchedulingDTO classData, List<Room> availableRooms,
                                   Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                   Subject codeSubject, Subject japaneseSubject, Subject koreanSubject) {
        List<ScheduleResultDTO> classSchedules = new ArrayList<>();
        
        switch (classData.getClassType()) {
            case LANGUAGE_ONLY:
                // Đảm bảo languageType không null cho lớp LANGUAGE_ONLY
                if (classData.getLanguageType() == null) {
                    throw new RuntimeException("Language type cannot be null for LANGUAGE_ONLY class: " + classData.getClassName());
                }
                
                scheduleLanguageOnlyClass(classData, availableRooms, roomSchedule, 
                    classData.getLanguageType() == LanguageType.JAPANESE ? japaneseSubject : koreanSubject, 
                    classSchedules);
                break;
                
            case CODE_ONLY:
                // Với lớp CODE_ONLY, languageType có thể null
                scheduleCodeOnlyClass(classData, availableRooms, roomSchedule, codeSubject, classSchedules);
                break;
                
            case COMBINED:
                // Đảm bảo languageType không null cho lớp COMBINED
                if (classData.getLanguageType() == null) {
                    throw new RuntimeException("Language type cannot be null for COMBINED class: " + classData.getClassName());
                }
                
                scheduleCombinedClass(classData, availableRooms, roomSchedule, codeSubject, 
                    classData.getLanguageType() == LanguageType.JAPANESE ? japaneseSubject : koreanSubject, 
                    classSchedules);
                break;
        }
        
        return classSchedules;
    }
    
    private void scheduleLanguageOnlyClass(SchedulingDTO classData, List<Room> availableRooms,
                                         Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                         Subject languageSubject, List<ScheduleResultDTO> classSchedules) {
        // Language-only classes fixed at 7:30-9:00
        
        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                .filter(r -> r.getId().equals(classData.getRoomId()))
                .findFirst()
                .orElse(null);
        }
        
        // Nếu không có phòng được chọn, tìm phòng khả dụng đầu tiên
        if (selectedRoom == null) {
            for (Room room : availableRooms) {
                if (isRoomAvailable(room, "MONDAY", LANGUAGE_START, LANGUAGE_END, roomSchedule)) {
                    selectedRoom = room;
                    break;
                }
            }
        }
        
        if (selectedRoom == null) {
            throw new RuntimeException("No available room for class: " + classData.getClassName());
        }
        
        // Xếp lịch vào ngày thứ 2, 4, 6 sáng sớm
        for (String day : Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY")) {
            if (isRoomAvailable(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule)) {
                // Đánh dấu phòng đã được sử dụng
                markRoomAsOccupied(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule);
                
                // Tạo lịch học
                ScheduleResultDTO scheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    languageSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    LANGUAGE_START,
                    LANGUAGE_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE
                );
                
                classSchedules.add(scheduleDTO);
            }
        }
    }
    
    private void scheduleCodeOnlyClass(SchedulingDTO classData, List<Room> availableRooms,
                                     Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                     Subject codeSubject, List<ScheduleResultDTO> classSchedules) {
        // Code-only classes flexible: morning (7:30-11:30) or afternoon (13:00-17:00)
        
        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                .filter(r -> r.getId().equals(classData.getRoomId()))
                .findFirst()
                .orElse(null);
        }
        
        // Nếu không có phòng được chọn, tìm phòng khả dụng đầu tiên
        if (selectedRoom == null) {
            for (Room room : availableRooms) {
                if (isRoomAvailable(room, "MONDAY", MORNING_START, MORNING_END, roomSchedule) ||
                    isRoomAvailable(room, "MONDAY", AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                    selectedRoom = room;
                    break;
                }
            }
        }
        
        if (selectedRoom == null) {
            throw new RuntimeException("No available room for class: " + classData.getClassName());
        }
        
        // Ưu tiên xếp lịch vào buổi sáng, nếu không được thì buổi chiều
        boolean scheduled = false;
        
        // Thử xếp lịch vào ngày thứ 3, 5 buổi sáng
        for (String day : Arrays.asList("TUESDAY", "THURSDAY")) {
            if (isRoomAvailable(selectedRoom, day, MORNING_START, MORNING_END, roomSchedule)) {
                // Đánh dấu phòng đã được sử dụng
                markRoomAsOccupied(selectedRoom, day, MORNING_START, MORNING_END, roomSchedule);
                
                // Tạo lịch học
                ScheduleResultDTO scheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    codeSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    MORNING_START,
                    MORNING_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE
                );
                
                classSchedules.add(scheduleDTO);
                scheduled = true;
            }
        }
        
        // Nếu không xếp được buổi sáng, thử buổi chiều
        if (!scheduled) {
            for (String day : Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY")) {
                if (isRoomAvailable(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                    // Đánh dấu phòng đã được sử dụng
                    markRoomAsOccupied(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);
                    
                    // Tạo lịch học
                    ScheduleResultDTO scheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        AFTERNOON_START,
                        AFTERNOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE
                    );
                    
                    classSchedules.add(scheduleDTO);
                    scheduled = true;
                    break;
                }
            }
        }
        
        if (!scheduled) {
            throw new RuntimeException("Could not schedule class: " + classData.getClassName());
        }
    }
    
    private void scheduleCombinedClass(SchedulingDTO classData, List<Room> availableRooms,
                                     Map<String, Map<Integer, Set<TimeSlot>>> roomSchedule,
                                     Subject codeSubject, Subject languageSubject, 
                                     List<ScheduleResultDTO> classSchedules) {
        // Combined classes with language in morning: 9:00-12:00
        // Combined classes with code in afternoon: 13:00-17:00
        
        // Tìm phòng học khả dụng, ưu tiên dùng phòng mà người dùng đã chọn (nếu có)
        Room selectedRoom = null;
        if (classData.getRoomId() != null) {
            selectedRoom = availableRooms.stream()
                .filter(r -> r.getId().equals(classData.getRoomId()))
                .findFirst()
                .orElse(null);
        }
        
        // Nếu không có phòng được chọn, tìm phòng khả dụng đầu tiên
        if (selectedRoom == null) {
            for (Room room : availableRooms) {
                if (isRoomAvailable(room, "MONDAY", LANGUAGE_START, LANGUAGE_END, roomSchedule) &&
                    isRoomAvailable(room, "MONDAY", AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                    selectedRoom = room;
                    break;
                }
            }
        }
        
        if (selectedRoom == null) {
            throw new RuntimeException("No available room for class: " + classData.getClassName());
        }
        
        boolean scheduled = false;
        
        // Thử xếp lịch cả ngày thứ 2, 4, 6
        for (String day : Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY")) {
            boolean canScheduleMorning = isRoomAvailable(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule);
            boolean canScheduleNoon = isRoomAvailable(selectedRoom, day, NOON_START, NOON_END, roomSchedule);
            boolean canScheduleAfternoon = isRoomAvailable(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);
            
            if (canScheduleMorning && canScheduleNoon && canScheduleAfternoon) {
                // Sáng sớm học ngoại ngữ
                markRoomAsOccupied(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule);
                ScheduleResultDTO langScheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    languageSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    LANGUAGE_START,
                    LANGUAGE_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE
                );
                classSchedules.add(langScheduleDTO);
                
                // Trưa học ngoại ngữ nâng cao
                markRoomAsOccupied(selectedRoom, day, NOON_START, NOON_END, roomSchedule);
                ScheduleResultDTO advLangScheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    "Advanced " + languageSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    NOON_START,
                    NOON_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE
                );
                classSchedules.add(advLangScheduleDTO);
                
                // Chiều học lập trình
                markRoomAsOccupied(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);
                ScheduleResultDTO codeScheduleDTO = createScheduleDTO(
                    classData.getClassName(),
                    codeSubject.getSubjectName(),
                    selectedRoom.getRoomName(),
                    day,
                    AFTERNOON_START,
                    AFTERNOON_END,
                    DEFAULT_START_DATE,
                    DEFAULT_END_DATE
                );
                classSchedules.add(codeScheduleDTO);
                
                scheduled = true;
                break;
            }
        }
        
        if (!scheduled) {
            // Nếu không xếp được cả ngày, thử xếp riêng lẻ từng buổi vào các ngày khác nhau
            
            // Xếp buổi sáng học ngoại ngữ
            for (String day : Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY")) {
                if (isRoomAvailable(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule)) {
                    markRoomAsOccupied(selectedRoom, day, LANGUAGE_START, LANGUAGE_END, roomSchedule);
                    ScheduleResultDTO langScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        languageSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        LANGUAGE_START,
                        LANGUAGE_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE
                    );
                    classSchedules.add(langScheduleDTO);
                    break;
                }
            }
            
            // Xếp buổi chiều học lập trình
            for (String day : Arrays.asList("TUESDAY", "THURSDAY")) {
                if (isRoomAvailable(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule)) {
                    markRoomAsOccupied(selectedRoom, day, AFTERNOON_START, AFTERNOON_END, roomSchedule);
                    ScheduleResultDTO codeScheduleDTO = createScheduleDTO(
                        classData.getClassName(),
                        codeSubject.getSubjectName(),
                        selectedRoom.getRoomName(),
                        day,
                        AFTERNOON_START,
                        AFTERNOON_END,
                        DEFAULT_START_DATE,
                        DEFAULT_END_DATE
                    );
                    classSchedules.add(codeScheduleDTO);
                    break;
                }
            }
        }
        
        if (classSchedules.isEmpty()) {
            throw new RuntimeException("Could not schedule combined class: " + classData.getClassName());
        }
    }
    
    private Subject getOrCreateSubject(String subjectName) {
        Optional<Subject> existingSubject = subjectRepository.findBySubjectName(subjectName);
        if (existingSubject.isPresent()) {
            return existingSubject.get();
        } else {
            Subject newSubject = new Subject();
            newSubject.setSubjectName(subjectName);
            newSubject.setDescription("Auto-generated subject for " + subjectName);
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
    
    private ScheduleResultDTO createScheduleDTO(String className, String subjectName, String roomName,
                                      String dayOfWeek, LocalTime startTime, LocalTime endTime,
                                      LocalDate startDate, LocalDate endDate) {
        ScheduleResultDTO dto = new ScheduleResultDTO();
        dto.setClassName(className);
        dto.setSubjectName(subjectName);
        dto.setRoomName(roomName);
        dto.setDayOfWeek(dayOfWeek);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        return dto;
    }


    public boolean saveSchedule(List<ScheduleResultDTO> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException("Danh sách lịch học không được để trống");
        }

        for (int i = 0; i < schedules.size(); i++) {
            ScheduleResultDTO dto = schedules.get(i);
            
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
            
            try {
                // Lưu schedule vào database
                scheduleRepository.save(schedule);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lưu lịch học cho lớp " + dto.getClassName() + ": " + e.getMessage(), e);
            }
        }
        
        return true;
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
} 