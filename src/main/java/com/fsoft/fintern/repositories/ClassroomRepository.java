package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    Optional<Classroom> findClassroomByClassName(String className);
    Optional<Classroom> findClassroomById(Integer classId);

    @Query("SELECT c FROM Classroom c WHERE c.manager.id = :employeeId")
    List<Classroom> findClassroomsByEmployeeId(Long employeeId);
}
