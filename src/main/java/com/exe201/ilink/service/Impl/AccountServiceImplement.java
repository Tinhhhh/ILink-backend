package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.exception.CustomSuccessHandler;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.TokenRepository;
import com.exe201.ilink.sercurity.JwtTokenProvider;
import com.exe201.ilink.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImplement implements AccountService {

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResponseEntity<Object> getCurrentAccountInfo(HttpServletRequest request) {

        //Extract Token From Header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No JWT found in request header");
        }

        //Extract Account Info
        String userEmail = jwtTokenProvider.getUsername(token);
        Account account = accountRepository.findByEmail(userEmail)
                .orElse(null);

        if (account == null && !jwtTokenProvider.validateToken(token)) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No account found with this token");
        }

        if (!jwtTokenProvider.isTokenValid(token, account.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or is expired");
        }

        return CustomSuccessHandler.responseBuilder(HttpStatus.OK,
                "Successfully retrieved user information",
                account);
    }
}
