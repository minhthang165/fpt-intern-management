package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.AttendanceStatus;

import java.time.LocalDate;

public class GetAttendanceDTO {
    private Integer classId;
    private Integer scheduleId;
    private String createAt;

    public GetAttendanceDTO(Integer classId, Integer scheduleId, String createAt) {
        this.classId = classId;
        this.scheduleId = scheduleId;
        this.createAt = createAt;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}
