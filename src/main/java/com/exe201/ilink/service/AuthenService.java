package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.model.payload.dto.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthenService {
    void register(RegistrationRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void activeAccount(String token, HttpServletResponse response);

    void logout(HttpServletResponse request, HttpServletResponse response, Authentication authentication);

    void refreshToken(HttpServletRequest request, HttpServletResponse response);

}
