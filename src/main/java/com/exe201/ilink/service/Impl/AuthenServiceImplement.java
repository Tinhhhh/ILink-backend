package com.exe201.ilink.service.Impl;

import com.exe201.ilink.exception.RegisterAccountExistedException;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.EmailToken;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.entity.Token;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.EmailTokenRepository;
import com.exe201.ilink.repository.RoleRepository;
import com.exe201.ilink.service.AuthenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenServiceImplement implements AuthenService {

    private final EmailTokenRepository emailTokenRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    @Value("${application.email.secure.characters}")
    private String emailSecureChar;

    @Override
    public void register(RegistrationRequest request) {
         Role accountRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .role(accountRole)
                .build();
        accountRepository.save(account);
        sendValidationEmail(account);
    }


    private void sendValidationEmail(Account account) {
        String validateToken = generateActiveToken(account);
        emai

    }

    private String generateActiveToken(Account account) {
        StringBuilder codeBuilder  = new StringBuilder();
        SecureRandom random = new SecureRandom();
        //tao ra 6 ki tu ngau nhien
        for (int i = 0; i < 6; i++) {
            //lay ngau nhien 1 ki tu trong chuoi emailSecureChar
            int randomIndex = random.nextInt(emailSecureChar.length());
            codeBuilder.append(emailSecureChar.charAt(randomIndex));
        }
        //luu token vao db
        String generatedToken = codeBuilder.toString();
        EmailToken token = EmailToken.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .revokedToken(false)
                .account(account)
                .build();
        emailTokenRepository.save(token);
        return generatedToken;
    }


}
