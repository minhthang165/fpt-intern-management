package com.fsoft.fintern.dtos;

public class FileDTO {

        private Integer id;
        private Integer submitterId;
        private String fileType;
        private String displayName;
        private String path;
        private Double size;

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

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
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

        public Double getSize() {
            return size;
        }

        public void setSize(Double size) {
            this.size = size;
        }
    }


