package com.fsoft.fintern.models;

import com.fsoft.fintern.models.EmbedableID.CompletedTaskId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "[completed_tasks]" , schema = "dbo")
public class CompletedTask {

    @EmbeddedId
    private CompletedTaskId id;

    @Column(name = "[file]", length = 255) // Use square brackets to escape the reserved keyword
    private String file;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // Default value

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Default to current time

    @Column(name = "mark")
    private Integer mark;

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    @Column(name = "is_active")
    private Boolean isActive = true; // Default to true (1)


    // Default constructor
    public CompletedTask() {}


    // Getters and Setters
    public CompletedTaskId getId() {
        return id;
    }

    public void setId(CompletedTaskId id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

}