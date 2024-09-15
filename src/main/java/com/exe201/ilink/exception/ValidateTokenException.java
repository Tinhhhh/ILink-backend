package com.exe201.ilink.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ValidateTokenException extends RuntimeException{

    private HttpStatus httpStatus;
    private String message;

}
