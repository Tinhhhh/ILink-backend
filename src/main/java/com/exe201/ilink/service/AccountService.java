package com.exe201.ilink.service;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.payload.dto.request.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AccountService {

    Account getCurrentAccountInfo(HttpServletRequest request);

    void changePassword(ChangePasswordRequest changePasswordRequest, HttpServletRequest request);

}
