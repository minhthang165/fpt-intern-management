package com.fsoft.fintern.repositories;
import com.fsoft.fintern.models.CompletedTask;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedTaskRepository extends JpaRepository<CompletedTask, CompletedTaskId> {
    @Query("SELECT ct FROM CompletedTask ct WHERE ct.id.taskId = :taskId AND ct.id.userId = :userId AND ct.id.classId = :classroomId")
    Optional<CompletedTask> findByTaskIdAndUserIdAndClassroomId(@Param("taskId") int taskId,
                                                                @Param("userId") int userId,
                                                                @Param("classroomId") int classroomId);


    @Query("SELECT ct FROM CompletedTask ct WHERE ct.id.taskId = :taskId AND ct.id.classId = :classId")
    List<CompletedTask> findByTaskIdAndClassroomId(@Param("taskId") int taskId, @Param("classId") int classId);
}

