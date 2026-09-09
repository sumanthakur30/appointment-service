package com.shopmanagement.ipdservice.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sugamflow.observability.error.ErrorCodes;
import com.sugamflow.observability.error.ErrorEnvelope;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), ErrorCodes.VALIDATION);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflict(IllegalStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage(), ErrorCodes.CONFLICT);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> forbidden(SecurityException ex) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, String errorCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        ErrorEnvelope.apply(body, errorCode != null ? errorCode : ErrorCodes.IPD);
        if (errorCode == null) {
            body.remove("errorCode");
        }
        return ResponseEntity.status(status).headers(ErrorEnvelope.headers()).body(body);
    }
}
