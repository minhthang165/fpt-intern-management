    package com.fsoft.fintern.models;

    import com.fsoft.fintern.enums.ClassStatus;
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

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = true, length = 50,  columnDefinition = "NVARCHAR(50) DEFAULT 'NOT_STARTED' CHECK ([status] IN ('NOT_STARTED', 'ON_GOING', 'ENDED'))")
        private ClassStatus status;

        @OneToOne
        @JoinColumn(name = "mentor_id",
                referencedColumnName = "id")
        private User manager;

        @Column(name = "class_description")
        private String classDescription;

        public String getClassDescription() {
            return classDescription;
        }

        public void setClassDescription(String classDescription) {
            this.classDescription = classDescription;
        }

        public ClassStatus getStatus() {
            return status;
        }

        public void setStatus(ClassStatus status) {
            this.status = status;
        }

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
