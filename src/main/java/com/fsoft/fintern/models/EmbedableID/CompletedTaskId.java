package com.fsoft.fintern.models.EmbedableID;


import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
public class CompletedTaskId implements Serializable {
    private Integer taskId;
    private Integer userId;

    public CompletedTaskId() {

    }

    public CompletedTaskId(Integer taskId, Integer userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

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
}