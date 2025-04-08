package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.AttendanceStatus;

public class AttendanceDTO {
    private Integer id;
    private Integer userId;
    private Integer scheduleId;
    private AttendanceStatus status;

    public AttendanceDTO() {
    }

    public AttendanceDTO(Integer id, Integer userId, Integer scheduleId, AttendanceStatus status) {
        this.id = id;
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.status = status;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}