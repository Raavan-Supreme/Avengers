package com.example.demo.dto;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
@Data
public class ApiResponse {
    private boolean error;
    private String message;
    private HttpStatus httpStatus;
    private Object data;

    public ApiResponse(boolean error, String message, HttpStatus httpStatus, Object data) {
        this.error = error;
        this.message = message;
        this.httpStatus = httpStatus;
        this.data = data;
        if (error) {
            log.error("Error : {}", message, httpStatus, data);
        } else {
            log.info("Info : {}", message, httpStatus, data);
        }
    }

}

