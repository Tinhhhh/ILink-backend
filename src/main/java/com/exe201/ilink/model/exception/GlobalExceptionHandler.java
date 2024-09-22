package com.exe201.ilink.model.exception;

import com.exe201.ilink.Util.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST.value())
                                .timestamp(DateUtil.formatTimestamp(new Date()))
                                .data(errors)
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

    @ExceptionHandler(ActivationCodeException.class)
    public ResponseEntity<ExceptionResponse> handleActivationCodeException(ActivationCodeException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST.value())
                                .timestamp(DateUtil.formatTimestamp(new Date()))
                                .message("Email verification failed.")
                                .error(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ILinkException.class)
    public ResponseEntity<ExceptionResponse> handleILinkException(ILinkException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST.value())
                                .timestamp(DateUtil.formatTimestamp(new Date()))
                                .message(exception.getMessage())
                                .build()
                );
    }

}
