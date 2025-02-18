package com.fsoft.fintern.dtos;

import java.sql.Timestamp;


public class TaskDTO {
    private String taskName;
    private String fileData;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer creatorId;
    private Integer classId;

    public Integer getCreator() {
        return creatorId;
    }

    public void setCreator(Integer creator) {
        this.creatorId = creator;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
