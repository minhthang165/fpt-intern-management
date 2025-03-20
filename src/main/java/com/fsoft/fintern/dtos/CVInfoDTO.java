package com.fsoft.fintern.dtos;

import com.fsoft.fintern.models.EmbedableID.CV_InfoId;

public class CVInfoDTO {
    private CV_InfoId id;
    private Float gpa;
    private String skill;
    private String education;

    public CVInfoDTO() {}

    public CVInfoDTO(CV_InfoId id, Float gpa, String skill, String education) {
        this.id = id;
        this.gpa = gpa;
        this.skill = skill;
        this.education = education;
    }

    public CV_InfoId getId() {
        return id;
    }

    public void setId(CV_InfoId id) {
        this.id = id;
    }

    public Float getGpa() {
        return gpa;
    }

    public void setGpa(Float gpa) {
        this.gpa = gpa;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }
}
