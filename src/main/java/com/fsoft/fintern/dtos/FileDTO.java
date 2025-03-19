package com.fsoft.fintern.dtos;

public class FileDTO {

        private Integer id;
        private Integer submitterId;
        private String displayName;
        private String path;
        private Boolean isActive;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getSubmitterId() {
            return submitterId;
        }

        public void setSubmitterId(Integer submitterId) {
            this.submitterId = submitterId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }


