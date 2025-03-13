package com.fsoft.fintern.dtos;

public class CVSubmitterDTO {
    private Integer recruitmentId;
    private Integer fileId;

    public CVSubmitterDTO() {}

    public CVSubmitterDTO(Integer recruitmentId, Integer fileId) {
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
