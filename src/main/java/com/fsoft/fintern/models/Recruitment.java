package com.fsoft.fintern.models;

import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Recruitment")
public class Recruitment extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String position;

    @Column(nullable = false)
    private Integer salary;

    @Column(nullable = false, length = 255)
    private String experience;

    @Column(nullable = false, length = 255)
    private String education;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private WorkForm workForm;

    @Column(nullable = false)
    private Integer totalSlot;

    @Column(nullable = false)
    private Integer availableSlot;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false)
    private Timestamp startTime;

    @Column(nullable = false)
    private Timestamp endTime;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id", insertable = false, updatable = false)
    private User createdBy;

    public enum WorkForm {
        PART_TIME, FULL_TIME
    }

    // Getters và Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public WorkForm getWorkForm() {
        return workForm;
    }

    public void setWorkForm(WorkForm workForm) {
        this.workForm = workForm;
    }

    public Integer getTotalSlot() {
        return totalSlot;
    }

    public void setTotalSlot(Integer totalSlot) {
        this.totalSlot = totalSlot;
    }

    public Integer getAvailableSlot() {
        return availableSlot;
    }

    public void setAvailableSlot(Integer availableSlot) {
        this.availableSlot = availableSlot;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
    }

