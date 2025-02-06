package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.Gender;
import com.fsoft.fintern.enums.Role;

public class UserDTO {
    private String email;
    private Integer class_id;
    private Role role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getClass_id() {
        return class_id;
    }

    public void setClass_id(Integer class_id) {
        this.class_id = class_id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
