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
        @Query("SELECT u.id, u.avatar_path, u.first_name, u.last_name, u.userName, a.status, a.id " +
                "FROM User u " +
                "LEFT JOIN Attendance a ON u.id = a.user.id AND a.schedule.id = :scheduleId " +
                "WHERE u.classroom.id = :classId ")
        List<Object[]> findUsersAttendanceByClassIdAndScheduleId(@Param("classId") Integer classId, @Param("scheduleId") Integer scheduleId);
}