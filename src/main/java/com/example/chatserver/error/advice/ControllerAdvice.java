package com.example.chatserver.error.advice;


import com.example.chatserver.error.ErrorResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResult> illgalExHandler(IllegalArgumentException e) {
        log.error("[execptionHandle] ex", e);
        ErrorResult errorResult = new ErrorResult("IllegalArgumentException", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResult> EntityNotFoundException(EntityNotFoundException e) {
        log.error("[execptionHandle] ex", e);
        ErrorResult errorResult = new ErrorResult("EntityNotFoundException", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }
}
