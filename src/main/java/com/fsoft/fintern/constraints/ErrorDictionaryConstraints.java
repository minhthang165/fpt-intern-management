package com.fsoft.fintern.constraints;

public enum ErrorDictionaryConstraints {

    // USER
    USER_NOT_FOUND("Can not find this user"),
    USERS_IS_EMPTY("There are no users"),
    USERS_ALREADY_EXISTS("There is already a user with this email address"),

    // CLASSROOM
    CLASS_ALREADY_EXISTS("There is already a class with this name"),
    CLASS_IS_EMPTY("There are no classes"),
    CLASS_NOT_EXISTS_ID("There is no class with this ID"),
    CLASS_NOT_EXISTS_NAME("There is no class with this name"),

    // ACTIVATE
    IS_ACTIVE_TRUE("This object is already true"),

    // AUTH
    CREATED_FOR_INTERN_ONLY("Role must be Intern"),
    CREATED_FOR_EMPLOYEE_OR_GUEST_ONLY("Role must be Employee or Guest"),

    // TASKS
    TASK_IS_EMPTY("There is no Task existed"),
    TASK_IS_ALREADY_EXISTS("There is already a task with this name"),
    TASK_NOT_EXISTS_ID("There is no task with this ID"),
    INVALID_TIME("Time not Valid"),
    IS_ACTIVE_FALSE("This object is not active"),
    TASK_IS_COMPLETED("This Task already Completed"),
    COMPLETED_TASK_IS_NOT_FOUND("Not find any Completed Task"),
    COMPLETED_TASK_IS_NOT_FOUND_ID("Not find any Completed Task ID");


    private final String message;

    ErrorDictionaryConstraints(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
