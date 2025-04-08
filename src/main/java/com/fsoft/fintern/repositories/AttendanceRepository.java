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
        @Query(value = "SELECT a.attendance_id, a.user_id, a.schedule_id, a.status FROM attendance a JOIN [user] u ON u.id = a.user_id WHERE a.is_active = 1 AND u.class_id = :classId", nativeQuery = true)
        List<Object[]> findAttendanceByClassId(@Param("classId") Integer classId);
}
