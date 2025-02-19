package com.fsoft.fintern.models;

import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;

@Entity
@Table(name = "\"User\"")
public class User extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    private String first_name;
    private String last_name;
    private String email;
    private String phone_number;

    @ManyToOne()
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private Classroom classroom;

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    @Column(name = "avatar_path", columnDefinition = "NVARCHAR(MAX) DEFAULT '/assets/img/user/default-avatar.png'")
    private String avatar_path;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = true, length = 50,  columnDefinition = "NVARCHAR(50) DEFAULT 'GUEST' CHECK (role IN ('ADMIN', 'EMPLOYEE', 'INTERN', 'GUEST'))")
    private Role role;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAvatar_path() {
        return avatar_path;
    }

    public void setAvatar_path(String avatar_path) {
        this.avatar_path = avatar_path;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
