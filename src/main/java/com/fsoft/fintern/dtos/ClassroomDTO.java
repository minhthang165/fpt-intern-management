package com.fsoft.fintern.dtos;


import com.fsoft.fintern.enums.ClassStatus;

public class ClassroomDTO {
    private String className;
    private Integer numberOfIntern;
    private Integer managerId;
    private String classDescription;
    private ClassStatus status;
    private Integer conversationId;

    public String getClassDescription() {return classDescription;}
    public void setClassDescription(String classDescription) {this.classDescription = classDescription;}

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

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }
}
