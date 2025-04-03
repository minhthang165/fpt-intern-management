package com.fsoft.fintern.dtos;

import java.sql.Date;
import java.sql.Time;

public class ScheduleDTO {
    private Integer id;
    private Integer classId;
    private Integer subjectId;
    private Integer roomId;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private Date startDate;
    private Date endDate;

    public ScheduleDTO() {
    }

    public ScheduleDTO(Integer id, Integer classId, Integer subjectId, Integer roomId, String dayOfWeek, 
                      Time startTime, Time endTime, Date startDate, Date endDate) {
        this.id = id;
        this.classId = classId;
        this.subjectId = subjectId;
        this.roomId = roomId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}