package com.fsoft.fintern.models;

import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;

@Entity
@Table(name = "CV_Info")
public class CVInfo extends BaseModel {
    @EmbeddedId
    private CVInfoId id;

    @Column(nullable = false)
    private Double gpa;

    @Column(nullable = false, length = 255)
    private String skill;

    @Column(nullable = false, length = 255)
    private String education;

    public CVInfoId getId() {
        return id;
    }

    public void setId(CVInfoId id) {
        this.id = id;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
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

