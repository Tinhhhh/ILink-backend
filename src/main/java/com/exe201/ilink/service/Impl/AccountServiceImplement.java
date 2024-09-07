package com.exe201.ilink.service.Impl;

import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImplement implements AccountService {

    private final AccountRepository accountRepository;

    public ResponseEntity<Object> getAccountInformation(HttpServletRequest request) {
        return null;
    }
}
