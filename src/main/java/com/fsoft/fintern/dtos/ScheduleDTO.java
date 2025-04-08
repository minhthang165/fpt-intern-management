package com.fsoft.fintern.dtos;

import java.time.LocalDate;
import java.time.LocalTime;


public class ScheduleDTO {
    private Integer id;
    private String className;
    private Integer mentorID;

    public Integer getMentorId() {
        return mentorID;
    }

    public void setMentorId(Integer mentorID) {
        this.mentorID = mentorID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public ScheduleDTO(Integer id, String className, String subjectName, String roomName, String dayOfWeek, LocalTime startTime, LocalTime endTime, LocalDate endDate, LocalDate startDate) {
        this.id = id;
        this.className = className;
        this.subjectName = subjectName;
        this.roomName = roomName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.endDate = endDate;
        this.startDate = startDate;
    }

    public ScheduleDTO() {
    }

    private String subjectName;
    private String roomName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate startDate;
    private LocalDate endDate;
} 