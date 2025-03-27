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

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "position", nullable = false, length = 255)
    private String position;

    @Column(name = "experience_requirement", nullable = false, length = 255)
    private String experienceRequirement;

    @Column(name = "language", length = 255)
    private String language;

    @Column(name = "min_GPA")
    private Float minGPA;

    @Column(name = "total_slot", nullable = false)
    private Integer totalSlot;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "end_time", nullable = false)
    private Timestamp endTime;
  
    @Column(name = "class_id")
    private Integer classId;
   
    @Transient
    private Integer applicationCount;
  
    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getExperienceRequirement() {
        return experienceRequirement;
    }

    public void setExperienceRequirement(String experienceRequirement) {
        this.experienceRequirement = experienceRequirement;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Float getMinGPA() {
        return minGPA;
    }

    public void setMinGPA(Float minGPA) {
        this.minGPA = minGPA;
    }

    public Integer getTotalSlot() {
        return totalSlot;
    }

    public void setTotalSlot(Integer totalSlot) {
        this.totalSlot = totalSlot;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Integer getApplicationCount() {
        return applicationCount != null ? applicationCount : 0;
    }

    public void setApplicationCount(Integer applicationCount) {
        this.applicationCount = applicationCount;
    }

    public Integer getRemainingSlot() {
        int applications = getApplicationCount();
        return totalSlot - applications;
    }
}

