package com.fsoft.fintern.services;

import com.fsoft.fintern.models.Schedule;
import com.fsoft.fintern.repositories.ScheduleRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    // Create
    @Transactional
    public Schedule createSchedule(@Valid @NotNull Schedule schedule) {
        validateSchedule(schedule);
        return scheduleRepository.save(schedule);
    }

    // Read (Get all)
    public ResponseEntity<List<Schedule>> findAll() {
        List<Schedule> schedules = this.scheduleRepository.findAll();
        if (schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }
    }


    // Tìm lịch học theo userId (người dùng thuộc lớp)
    public List<Schedule> findSchedulesByUserId(Integer userId) {
        List<Schedule> allSchedules = this.scheduleRepository.findAll();

        // Lọc lịch học theo mentorid
        List<Schedule> classSchedules = allSchedules.stream()
                .filter(schedule -> {
                    // Kiểm tra xem classField có tồn tại không
                    if (schedule.getMentor() != null) {
                        // Kiểm tra xem classId có khớp không
                        if (schedule.getMentor().getId().equals(userId)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        return classSchedules;
    }

    // Tìm lịch học theo ngày
    public List<Schedule> findSchedulesByDate(LocalDate date) {
        List<Schedule> allSchedules = this.scheduleRepository.findAll();
      
        // Lọc lịch học theo ngày (lịch học có startDate <= date <= endDate)
        List<Schedule> schedulesByDate = allSchedules.stream()
                .filter(schedule -> {
                    LocalDate startDate = schedule.getStartDate();
                    LocalDate endDate = schedule.getEndDate();

                    return (startDate == null || !date.isBefore(startDate)) && 
                           (endDate == null || !date.isAfter(endDate));
                })
                .collect(Collectors.toList());
        
        return schedulesByDate;
    }

    public ResponseEntity<Page<Schedule>> findAllWithPagination(Pageable pageable) throws BadRequestException {
        Page<Schedule> schedules = this.scheduleRepository.findAll(pageable);
        if (schedules.isEmpty()) {
            throw new BadRequestException("No subjects found");
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }
    }
    // Read (Get by ID)
    public Schedule getScheduleById(@NotNull Integer id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }

    // Update
    @Transactional
    public Schedule updateSchedule(@NotNull Integer id, @Valid @NotNull Schedule scheduleDetails) {
        Schedule existingSchedule = getScheduleById(id);

        // Update fields
        existingSchedule.setClassField(scheduleDetails.getClassField());
        existingSchedule.setSubject(scheduleDetails.getSubject());
        existingSchedule.setRoom(scheduleDetails.getRoom());

        existingSchedule.setStartTime(scheduleDetails.getStartTime());
        existingSchedule.setEndTime(scheduleDetails.getEndTime());
        existingSchedule.setStartDate(scheduleDetails.getStartDate());
        existingSchedule.setEndDate(scheduleDetails.getEndDate());

        validateSchedule(existingSchedule);
        return scheduleRepository.save(existingSchedule);
    }

    // Delete
    @Transactional
    public void deleteSchedule(@NotNull Integer id) {
        Schedule schedule = getScheduleById(id);
        scheduleRepository.delete(schedule);
    }

    // Additional query methods
    public List<Schedule> findSchedulesByClassId(@NotNull Integer classId) {
        return scheduleRepository.findByClassFieldId(classId);
    }

    public List<Schedule> findSchedulesBySubjectId(@NotNull Integer subjectId) {
        return scheduleRepository.findBySubjectId(subjectId);
    }

    public List<Schedule> findSchedulesByRoomId(@NotNull Integer roomId) {
        return scheduleRepository.findByRoomId(roomId);
    }


    // Validation method
    private void validateSchedule(Schedule schedule) {
        // Check if end time is after start time
        if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check if all required relationships are present
        if (schedule.getClassField() == null || schedule.getSubject() == null || schedule.getRoom() == null) {
            throw new IllegalArgumentException("Classroom, Subject, and Room must be specified");
        }
    }
}