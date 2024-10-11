package com.exe201.ilink.service;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.payload.dto.request.AccountProfile;
import com.exe201.ilink.model.payload.dto.request.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AccountService {

    Account getCurrentAccountInfo(HttpServletRequest request);

    void changePassword(ChangePasswordRequest changePasswordRequest, HttpServletRequest request);

    void updateAccountProfilePicture(UUID id, String imageURLMain);

    void updateAccountInfo(UUID id, AccountProfile accountProfile);
}
