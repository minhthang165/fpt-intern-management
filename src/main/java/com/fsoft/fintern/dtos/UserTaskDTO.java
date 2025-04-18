package com.fsoft.fintern.dtos;

import com.fsoft.fintern.models.CompletedTask;
import com.fsoft.fintern.models.User;

public class UserTaskDTO {
    private User user;
    private CompletedTask completedTask;

    public UserTaskDTO(User user, CompletedTask completedTask) {
        this.user = user;
        this.completedTask = completedTask;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CompletedTask getCompletedTask() {
        return completedTask;
    }

    public void setCompletedTask(CompletedTask completedTask) {
        this.completedTask = completedTask;
    }
}
