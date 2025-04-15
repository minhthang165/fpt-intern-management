package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CompletedTasks;


import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


@Repository

public interface CompletedTaskRepository extends JpaRepository<CompletedTasks, CompletedTaskId> {
    CompletedTaskId task(Task task);
    List<CompletedTasks> user(User user);
}
