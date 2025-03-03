package com.fsoft.fintern.models;

import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Entity
@Table(name = "Class")
public class Classroom extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "class_name")
    private String className;

    @Column(name = "number_of_interns", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer number_of_interns = (Integer) 0;

    @OneToOne
    @JoinColumn(name = "manager_id",
            referencedColumnName = "id")

    private User manager;

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getNumber_of_interns() {
        return number_of_interns;
    }

    public void setNumber_of_interns(Integer number_of_interns) {
        this.number_of_interns = number_of_interns;
    }
}
