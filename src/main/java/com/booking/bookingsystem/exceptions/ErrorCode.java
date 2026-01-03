package com.booking.bookingsystem.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {

    //Validation
    VALIDATION_ERROR("ERR_001", "Validation failed"),

    //Not found
    RESOURCE_NOT_FOUND("ERR_002", "Resource not found"),

    //Conflict
    EMAIL_ALREADY_EXISTS("ERR_003", "Email already exists"),

    //Authentication
    INVALID_CREDENTIALS("ERR_004", "Invalid credentials"),

    //Authorization
    FORBIDDEN("ERR_005", "Access forbidden"),

    //Generic
    INTERNAL_ERROR("ERR_999", "Internal server error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message){
        this.code = code;
        this.message = message;
    }

}
