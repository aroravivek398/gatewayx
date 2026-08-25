package com.gatewayx.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFound(PlanNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DeveloperNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeveloperNotFound(DeveloperNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "DEVELOPER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(ApiKeyLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimitExceeded(ApiKeyLimitExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "API_KEY_LIMIT_EXCEEDED", ex.getMessage(), request);
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request);
    }
    @ExceptionHandler(ApiKeyOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyOwnership(ApiKeyOwnershipException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "API_KEY_ACCESS_DENIED", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(errorResponse);
    }
}