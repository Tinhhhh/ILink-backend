package com.exe201.ilink.model.exception;

public class RegisterAccountExistedException extends RuntimeException{
    public RegisterAccountExistedException(String message) {
        super(message);
    }
}
