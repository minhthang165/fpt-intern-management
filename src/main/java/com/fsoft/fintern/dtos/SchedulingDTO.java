package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;


public class SchedulingDTO {
    private String classId;
    private String className;
    private ClassType classType;
    private LanguageType languageType;
    private Integer roomId;
    private Integer codeMentorId;
    private Integer languageMentorId;

    public SchedulingDTO(String classId, String className, ClassType classType, LanguageType languageType,
                         Integer roomId, Integer codeMentorId, Integer languageMentorId) {
        this.classId = classId;
        this.className = className;
        this.languageType = languageType;
        this.classType = classType;
        this.roomId = roomId;
        this.codeMentorId = codeMentorId;
        this.languageMentorId = languageMentorId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
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

    public Integer getCodeMentorId() {
        return codeMentorId;
    }

    public void setCodeMentorId(Integer codeMentorId) {
        this.codeMentorId = codeMentorId;
    }

    public Integer getLanguageMentorId() {
        return languageMentorId;
    }

    public void setLanguageMentorId(Integer languageMentorId) {
        this.languageMentorId = languageMentorId;
    }
}