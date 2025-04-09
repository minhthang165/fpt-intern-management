package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SchedulingDTO {

    public SchedulingDTO(String classId, String className, ClassType classType, LanguageType languageType, Integer roomId) {
        this.classId = classId;
        this.className = className;
        this.classType = classType;
        this.languageType = languageType;
        this.roomId = roomId;
    }

    public SchedulingDTO() {
    }

    private String classId;
    private String className;
    private ClassType classType;
    private LanguageType languageType;
    private Integer roomId;

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public LanguageType getLanguageType() {
        return languageType;
    }

    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}