package com.resale.resaleoffer.shared;

import com.resale.resaleoffer.utils.ReturnObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReturnObject<Void>> handleAllExceptions(Exception ex) {
        ReturnObject<Void> response = new ReturnObject<>();
        System.out.println("HandleAllExceptions : " + ex.getMessage());
        response.setStatus(false);
        response.setMessage("Something went wrong");
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ReturnObject<Void>> handlePermissionDenied(PermissionDeniedException ex) {
        ReturnObject<Void> response = new ReturnObject<>();
        System.out.println("HandlePermissionDenied : " + ex.getMessage());
        response.setMessage(ex.getMessage());
        response.setStatus(false);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ReturnObject<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        ReturnObject<Object> response = new ReturnObject<>();
        response.setStatus(false);
        response.setMessage("Validation failed");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler({ IllegalArgumentException.class })
    public ResponseEntity<ReturnObject<Void>> handleBadRequest(Exception ex) {

        ReturnObject<Void> response = new ReturnObject<>();
        response.setStatus(false);
        response.setMessage(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ReturnObject<Void>> handleNotFound(ResourceNotFoundException ex) {

        ReturnObject<Void> response = new ReturnObject<>();
        response.setStatus(false);
        response.setMessage(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}



