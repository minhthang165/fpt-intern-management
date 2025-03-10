package com.fsoft.fintern.models;

import com.fsoft.fintern.models.EmbedableID.CVSubmitterId;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "CV_Submitter")
public class CVSubmitter {
    @EmbeddedId
    private CVSubmitterId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    public CVSubmitter() {}

    public CVSubmitter(CVSubmitterId id) {
        this.id = id;
    }

    public CVSubmitterId getId() {
        return id;
    }

    public void setId(CVSubmitterId id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
