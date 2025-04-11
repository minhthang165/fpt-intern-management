package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.AttendanceStatus;

public class AttendanceDTO {
    private Integer userId;
    private Integer scheduleId;
    private AttendanceStatus status;

    public AttendanceDTO() {
    }

    public AttendanceDTO(Integer userId, Integer scheduleId, String status) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.status = AttendanceStatus.valueOf(status);
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