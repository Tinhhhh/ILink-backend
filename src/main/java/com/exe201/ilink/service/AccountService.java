package com.exe201.ilink.service;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.payload.request.AccountProfile;
import com.exe201.ilink.model.payload.request.ChangePasswordRequest;
import com.exe201.ilink.model.payload.response.AccountInfoResponse;
import com.exe201.ilink.model.payload.response.ListAccountInfo;
import com.exe201.ilink.model.payload.response.UpdateAccountResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AccountService {

    Account getCurrentAccountInfo(HttpServletRequest request);

    void changePassword(ChangePasswordRequest changePasswordRequest, HttpServletRequest request);

    void updateAccountProfilePicture(UUID id, String imageURLMain);

    void updateAccountInfo(UUID id, AccountProfile accountProfile);

    ListAccountInfo getAllAccount(int pageNo, int pageSize, ProductSort sortBy, String keyword, String role);

    AccountInfoResponse getAccountByAdmin(UUID accountId);

    void editAccountByAdmin(UUID accountId, UpdateAccountResponse updateAccountResponse);
}
