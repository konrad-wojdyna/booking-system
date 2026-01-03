package com.booking.bookingsystem.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> errors
) {

    public ErrorResponse(int status, String error, String code, String message, String path){
        this(LocalDateTime.now(), status, error, code, message, path, null);
    }

    public ErrorResponse(int status, String error, String code, String message, String path, Map<String, String> errors){
        this(LocalDateTime.now(), status, error, code, message, path, errors);
    }
}
