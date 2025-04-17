package com.fsoft.fintern.services;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.AttendanceDTO;
import com.fsoft.fintern.dtos.ClassroomDTO;
import com.fsoft.fintern.enums.AttendanceStatus;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.*;
import com.fsoft.fintern.repositories.AttendanceRepository;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.ScheduleRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AttendanceService {
    private final AttendanceRepository attendance_Repository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository, ScheduleRepository scheduleRepository, ClassroomRepository classroomRepository) {
        this.attendance_Repository = attendanceRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.classroomRepository = classroomRepository;
    }

    public ResponseEntity<Attendance> createAttendance(AttendanceDTO attendanceDTO) throws BadRequestException {
        User user = this.userRepository.findById(attendanceDTO.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorDictionaryConstraints.USERS_ALREADY_EXISTS.getMessage()));
        if (user.getRole() != Role.INTERN) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        Schedule schedule = this.scheduleRepository.findById(attendanceDTO.getScheduleId())
                .orElseThrow(() -> new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage()));

        Integer mentorId = schedule.getMentor().getId();
        if (mentorId == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }


        Attendance newAttendance = new Attendance();
        newAttendance.setSchedule(schedule);
        newAttendance.setUser(user);
        newAttendance.setStatus(attendanceDTO.getStatus());
        newAttendance.setCreatedBy(mentorId);

        Attendance savedAttendance = attendance_Repository.save(newAttendance);
        return new ResponseEntity<>(savedAttendance, HttpStatus.CREATED);
    }

    public ResponseEntity<List<Attendance>> findAll(AttendanceDTO attendanceDTO) throws BadRequestException {
        List<Attendance> attendances = attendance_Repository.findAll();

        if (attendances.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.ATTENDANCE_IS_EMPTY.getMessage());
        }

        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }




    public ResponseEntity<Attendance> updateAttendancetoPresent(int userId, int scheduleId) throws BadRequestException {

        Attendance attendance = this.attendance_Repository.findByUserIdAndScheduleId(userId, scheduleId).orElseThrow(() ->
                new BadRequestException(ErrorDictionaryConstraints.ATTENDANCE_IS_EMPTY.getMessage())
        );
        attendance.setStatus(AttendanceStatus.PRESENT);

        this.attendance_Repository.save(attendance);

        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }



    public ResponseEntity<Attendance> updateAttendanceToAbsent(int userId, int scheduleId) throws BadRequestException {
        Attendance attendance = this.attendance_Repository.findByUserIdAndScheduleId(userId, scheduleId).orElseThrow(() ->
                new BadRequestException(ErrorDictionaryConstraints.ATTENDANCE_IS_EMPTY.getMessage())
        );
        attendance.setStatus(AttendanceStatus.ABSENT);

        this.attendance_Repository.save(attendance);

        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }




    public ResponseEntity<Attendance> delete(int id) throws BadRequestException {
        Attendance attendance = this.attendance_Repository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage())
        );
        attendance.setActive(false);
        attendance.setDeletedAt(Timestamp.from(Instant.now().plus(Duration.ofHours(7))));

        this.attendance_Repository.save(attendance);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }


    public ResponseEntity<Attendance> findById(int id) throws BadRequestException {
        Optional<Attendance> attendances = this.attendance_Repository.findById(id);
        if(attendances.isPresent()) {
            return new ResponseEntity<>(attendances.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }
    }

    public ResponseEntity<List<Object[]>> findUsersAttendanceByClassIdAndScheduleId(int classId, int scheduleId) throws BadRequestException {

        if (!classroomRepository.existsById(classId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        // Check if schedule exists
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.SCHEDULE_NOT_EXISTS_ID.getMessage());
        }

        List<Object[]> results = this.attendance_Repository.findUsersAttendanceByClassIdAndScheduleId(classId, scheduleId);

        if (results.isEmpty()) {
            throw new BadRequestException("No users found in this class");
        }

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    public ResponseEntity<List<Object[]>> findUsersAttendanceByClassIdAndUserIdAndScheduleId(int classId, int scheduleId, int userId) throws BadRequestException {

        if (!classroomRepository.existsById(classId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        // Check if schedule exists
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.SCHEDULE_NOT_EXISTS_ID.getMessage());
        }

        if (!userRepository.existsById(userId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }

        List<Object[]> results = this.attendance_Repository.findUsersAttendanceByClassIdAndUserIdAndScheduleId(classId, scheduleId, userId);

        if (results.isEmpty()) {
            throw new BadRequestException("Not found this User in this class");
        }

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    public ResponseEntity<List<Object[]>> findUsersAttendanceByClassIdAndScheduleIdAndDate(int classId, int scheduleId, String createAt) throws BadRequestException {

        LocalDate localDate = LocalDate.parse(createAt);
        if (!classroomRepository.existsById(classId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        // Check if schedule exists
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BadRequestException(ErrorDictionaryConstraints.SCHEDULE_NOT_EXISTS_ID.getMessage());
        }

        List<Object[]> results = this.attendance_Repository.findUsersAttendanceByClassIdAndScheduleIdAndDate(classId, scheduleId, localDate);

        if (results.isEmpty()) {
            throw new BadRequestException("No users found in this class");
        }

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}


