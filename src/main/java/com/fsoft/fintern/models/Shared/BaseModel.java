package com.fsoft.fintern.models.Shared;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseModel {
    @Column(name = "is_active")
    private boolean isActive = true;

    private Timestamp created_at;
    private Timestamp deleted_at;
    private Timestamp updated_at;
    private Integer created_by;
    private Integer updated_by;
    private Integer deleted_by;

    @PrePersist
    public void prePersist() {
        this.created_at = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    @PreRemove
    public void preRemove() {
        this.deleted_at = Timestamp.from(Instant.now().plus(Duration.ofHours(7)));
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.created_at = createdAt;
    }

    public Timestamp getDeletedAt() {
        return deleted_at;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deleted_at = deletedAt;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updated_at = updatedAt;
    }

    public Integer getCreatedBy() {
        return created_by;
    }

    public void setCreatedBy(Integer createdBy) {
        this.created_by = createdBy;
    }

    public Integer getUpdatedBy() {
        return updated_by;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updated_by = updatedBy;
    }

    public Integer getDeletedBy() {
        return deleted_by;
    }

    public void setDeletedBy(Integer deletedBy) {
        this.deleted_by = deletedBy;
    }
}
