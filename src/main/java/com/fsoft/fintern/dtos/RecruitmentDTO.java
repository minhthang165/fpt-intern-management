package com.fsoft.fintern.dtos;

import java.sql.Timestamp;

public class RecruitmentDTO {
    private Integer id;
    private String name;
    private String position;
    private String experience;
    private String language;
    private Float minGPA;
    private Integer totalSlot;
    private Integer availableSlot;
    private String description;
    private Timestamp endTime;

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

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}