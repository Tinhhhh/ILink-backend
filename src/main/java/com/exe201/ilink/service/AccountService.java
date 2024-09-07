package com.exe201.ilink.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<Object> getAccountInformation(HttpServletRequest request);
}
