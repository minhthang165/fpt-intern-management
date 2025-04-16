package com.fsoft.fintern.models.EmbedableID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CompletedTaskId implements Serializable {
    @Column(name = "task_id", nullable = false)
    private Integer taskId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    // Default constructor
    public CompletedTaskId() {}

    // Parameterized constructor
    public CompletedTaskId(Integer taskId, Integer userId, Integer classId) {
        this.taskId = taskId;
        this.userId = userId;
        this.classId = classId;
    }

    // Getters and Setters
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    // Equals and HashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedTaskId that = (CompletedTaskId) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(classId, that.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId, classId);
    }
}