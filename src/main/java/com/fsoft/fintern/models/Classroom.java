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

        @Column(name = "number_of_interns", columnDefinition = "INT DEFAULT 0")
        private Integer numberOfIntern = (Integer) 0;

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

        public Integer getNumberOfIntern() {
            return numberOfIntern;
        }

        public void setNumberOfIntern(Integer numberOfIntern) {
            this.numberOfIntern = numberOfIntern;
        }
    }
