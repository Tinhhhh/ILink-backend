package com.exe201.ilink.exception;

import com.exe201.ilink.Util.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        exception.printStackTrace();
        return  ResponseEntity
                .status(500)
                .body(
                        ExceptionResponse.builder()
                                .httpStatus(500)
                                .timestamp(DateUtil.formatTimestamp(new Date()))
                                .message("Internal Server Error. Please contact administrator for more information.")
                                .error(exception.getMessage())
                                .build()
                );

    }

    @ExceptionHandler(RegisterAccountExistedException.class)
    public ResponseEntity<ExceptionResponse> handleRegisterAccountExistedException(RegisterAccountExistedException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST.value())
                                .timestamp(DateUtil.formatTimestamp(new Date()))
                                .message("Registration request failed. Account already existed.")
                                .error(exception.getMessage())
                                .build()
                );
    }

}
