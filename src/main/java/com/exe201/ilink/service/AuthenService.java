package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;

public interface AuthenService {
    void register(RegistrationRequest request);
}
