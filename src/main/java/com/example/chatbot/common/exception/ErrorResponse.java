package com.example.chatbot.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldError> errors;

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<FieldError> errors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<FieldError> getErrors() { return errors; }

    public static class FieldError {
        private final String field;
        private final Object rejectedValue;
        private final String reason;

        public FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public String getField() { return field; }
        public Object getRejectedValue() { return rejectedValue; }
        public String getReason() { return reason; }
    }
}

