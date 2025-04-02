package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    Optional<Classroom> findClassroomByClassName(String className);
    Optional<Classroom> findClassroomById(Integer classId);
    @Query(value = "SELECT COUNT(u.id) FROM [user] u WHERE u.class_id = :classId AND u.is_active = 1", nativeQuery = true)
    Integer countUsersByClassIdAndIsActiveTrue(@Param("classId") Integer classId);
    @Query(value = "SELECT * FROM [user] WHERE class_id = :classId AND is_active = 1", nativeQuery = true)
    List<User> findByClassIdAndIsActiveTrue(@Param("classId") Integer classId);
    @Query("SELECT c FROM Classroom c WHERE c.manager.id = :mentorId")
    List<Classroom> findClassroomsByMentorId(@Param("mentorId") Integer mentorId);
    @Query(value = "SELECT * FROM Tasks WHERE class_id = :classId", nativeQuery = true)
    List<Task> findTaskIdByClassId(@Param("classId") Integer classId);

}
