package com.exe201.ilink.exception;

public class RegisterAccountExistedException extends RuntimeException{
    public RegisterAccountExistedException(String message) {
        super(message);
    }
}
