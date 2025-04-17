package com.fsoft.fintern.models;

import com.fsoft.fintern.enums.TaskStatus;
import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Tasks")
public class Task extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "start_time", nullable = false)
    private Timestamp startTime;

    @Column(name = "end_time", nullable = false)
    private Timestamp endTime;

    @Column(name = "[file]")
    private String fileData;

    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private Classroom classroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true, length = 50,  columnDefinition = "NVARCHAR(50) CHECK ([status] in ('PENDING', 'IN_PROGRESS', 'COMPLETED')) DEFAULT 'PENDING'")
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Getters và Setters
    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }


    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
