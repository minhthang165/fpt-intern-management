package com.fsoft.fintern.repositories;

import com.fsoft.fintern.dtos.AttendanceDTO;
import com.fsoft.fintern.models.Attendance;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
        Optional<Attendance> findAttendanceById(Integer attendanceId);
        Optional<Attendance> findByUserIdAndScheduleId(Integer userId, Integer scheduleId);
        @Query("SELECT a FROM Attendance a " +
                "LEFT JOIN a.user u " +
                "JOIN u.classroom c " +
                "JOIN Schedule s on s.classField.id = c.id " +
                "WHERE c.id = :classId AND s.id = :scheduleId")
        List<Attendance> findAttendanceByClassIdAndScheduleId(@Param("classId") Integer classId, @Param("scheduleId") Integer scheduleId);
}