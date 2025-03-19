package com.fsoft.fintern.dtos;


import com.fsoft.fintern.enums.ClassStatus;

public class ClassroomDTO {
    private String className;
    private Integer numberOfIntern;
    private Integer managerId;
    private ClassStatus status;

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getNumberOfIntern() {
        return numberOfIntern;
    }

    public void setNumberOfIntern(Integer numberOfIntern) {
        this.numberOfIntern = numberOfIntern;
    }
}
