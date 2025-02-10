package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;

public class UserDTO {
    private String email;
    private Integer classId;
    private Role role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
