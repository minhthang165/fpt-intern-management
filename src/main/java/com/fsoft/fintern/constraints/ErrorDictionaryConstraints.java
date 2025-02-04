package com.fsoft.fintern.constraints;

public enum ErrorDictionaryConstraints {

    //USER
    USER_NOT_FOUND("Can not find this user"),
    USERS_IS_EMPTY("There are no users"),
    CREATED_FOR_INTERN_ONLY("Role must be Intern"),
    CREATED_FOR_EMPLOYEE_OR_GUEST_ONLY("Role must be Employee or Guest");

    private final String message;

    ErrorDictionaryConstraints(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
