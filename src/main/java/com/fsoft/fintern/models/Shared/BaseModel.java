package com.fsoft.fintern.models.Shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseModel {
    @Column(name = "is_active")
    private boolean isActive = true;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "deleted_at")
    private Timestamp deletedAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_by")
    private Integer updatedBy;
    @Column(name = "deleted_by")
    private Integer deletedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    @PreRemove
    public void preRemove() {
        this.deletedAt = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(int deletedBy) {
        this.deletedBy = deletedBy;
    }
}
