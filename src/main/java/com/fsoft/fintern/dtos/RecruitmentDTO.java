package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.WorkForm;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RecruitmentDTO {
    private String position;
    private Integer salary;
    private String experience;
    private String education;
    private WorkForm workForm;
    private Integer totalSlot;
    private Integer availableSlot;
    private String description;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer creatorId;


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

    public void setCreator(Integer creator) {
        this.creatorId = creator;
    }
    public Integer getCreator() {
        return creatorId;
    }
}
