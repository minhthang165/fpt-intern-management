package com.fsoft.fintern.models.EmbedableID;

import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
public class CVInfoId implements Serializable {
    private Integer recruitmentId;
    private Integer fileId;
    public CVInfoId() {}
    public CVInfoId(Integer recruitmentId, Integer fileId) {
        this.recruitmentId = recruitmentId;
        this.fileId = fileId;
    }


    public Integer getRecruitmentId() {
        return recruitmentId;
    }

    public void setRecruitmentId(Integer recruitmentId) {
        this.recruitmentId = recruitmentId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }
}