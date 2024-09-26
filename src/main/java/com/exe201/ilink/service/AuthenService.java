package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.model.payload.dto.response.AuthenticationResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.security.NoSuchAlgorithmException;

public interface AuthenService {
    void register(RegistrationRequest request) throws MessagingException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void activeAccount(String token, HttpServletResponse response) throws MessagingException;

    void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication);

    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(String email) throws NoSuchAlgorithmException, MessagingException;

    void resetPassword(String email, String token);
}
