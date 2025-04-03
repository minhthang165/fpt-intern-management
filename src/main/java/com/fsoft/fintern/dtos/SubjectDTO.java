package com.fsoft.fintern.dtos;

public class SubjectDTO {
    private Integer id;
    private String subjectName;
    private String description;

    public SubjectDTO() {
    }

    public SubjectDTO(Integer id, String subjectName, String description) {
        this.id = id;
        this.subjectName = subjectName;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}