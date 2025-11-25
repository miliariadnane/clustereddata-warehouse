package com.bloomberg.clustereddata.config;

import com.bloomberg.clustereddata.dto.ApiError;
import com.bloomberg.clustereddata.exception.DealAlreadyExistsException;
import com.bloomberg.clustereddata.exception.InvalidCsvException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> details =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> "%s %s".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint violation", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleNotReadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        String detail = Optional.of(exception.getMostSpecificCause())
                .map(Throwable::getMessage)
                .orElse(exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Malformed request body", request, List.of(detail));
    }

    @ExceptionHandler(DealAlreadyExistsException.class)
    ResponseEntity<ApiError> handleDuplicate(DealAlreadyExistsException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidCsvException.class)
    ResponseEntity<ApiError> handleInvalidCsv(InvalidCsvException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleGeneric(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error", exception);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request, List.of(exception.getMessage()));
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .details(details)
                .build();
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }
}

