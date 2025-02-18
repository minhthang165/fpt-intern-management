package com.fsoft.fintern.dtos;

public class FeedbackDTO {

    private String content;
    private Integer created_by;


    public Integer getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Integer created_by) {
        this.created_by = created_by;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
