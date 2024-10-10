package com.exe201.ilink.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ILinkException extends RuntimeException {

    private HttpStatus httpStatus;

    public ILinkException(String message) {
        super(message);
    }

    public ILinkException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }


}
