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

        @Query("SELECT a FROM Attendance a WHERE a.user.classroom.id = :classId AND a.isActive = true")
        List<Attendance> findAttendanceByClassId(@Param("classId") Integer classId);
}
