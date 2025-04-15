package com.fsoft.fintern.models;

import com.fsoft.fintern.enums.TaskStatus;
import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;

@Entity
@Table(name = "Completedtasks")
public class CompletedTasks extends BaseModel {

    @EmbeddedId
    private CompletedTaskId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50, columnDefinition  = "NVARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'REJECTED'))")
    private TaskStatus status;


    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "comment")
    private String comment;

    @Column(name = "[file]")
    private String fileData;



    public CompletedTaskId getId() {
        return id;
    }

    public void setId(CompletedTaskId id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}