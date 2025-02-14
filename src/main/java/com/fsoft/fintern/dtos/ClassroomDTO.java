package com.fsoft.fintern.dtos;

import java.util.List;

public class ClassroomDTO {
    private String class_name;
    private Integer managerId;

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }
}
