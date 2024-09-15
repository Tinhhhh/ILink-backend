package com.exe201.ilink.service.Impl;

import com.exe201.ilink.exception.ActivationCodeException;
import com.exe201.ilink.exception.RegisterAccountExistedException;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.EmailToken;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.model.payload.dto.response.AuthenticationResponse;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.EmailTokenRepository;
import com.exe201.ilink.repository.RoleRepository;
import com.exe201.ilink.service.AuthenService;
import com.exe201.ilink.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenServiceImplement implements AuthenService {

    private final EmailTokenRepository emailTokenRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    @Value("${application.email.secure.characters}")
    private String emailSecureChar;

//    private static final Logger logger = LoggerFactory.getLogger(LogoutServiceConfig.class);

    @Override
    public void register(RegistrationRequest request) {
         Role accountRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        Account account = Account.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .gender("")
                .dob(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .isLocked(false)
                .isEnable(false)
                .role(accountRole)
                .build();
        accountRepository.save(account);
        sendValidationEmail(account);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return null;
    }

    @Override
    public void activeAccount(String token, HttpServletResponse response) {

        EmailToken savedCode = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new ActivationCodeException("active code not found"));

        if (savedCode.getValidateAt() != null) {
            throw new ActivationCodeException("Your account is already activated");
        }

        if (savedCode.isRevokedToken()) {
            throw new ActivationCodeException("This activation code is invalid as it has been revoked. Please use the latest activation code sent to your email.");
        }

        //check if code is expired
        if (LocalDateTime.now().isAfter(savedCode.getExpiredAt())) {
            savedCode.setRevokedToken(true);
            emailTokenRepository.save(savedCode);
            sendValidationEmail(savedCode.getAccount());
            throw new ActivationCodeException("Activation code has expired. A new code has been sent to your email address");
        }

        Account account = accountRepository.findById(savedCode.getAccount().getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        account.setEnable(true);
        accountRepository.save(account);
        savedCode.setValidateAt(LocalDateTime.now());
        savedCode.setRevokedToken(true);
        emailTokenRepository.save(savedCode);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/plain");
//        try {
//            response.getWriter().write("Account verification successfully");
//        } catch (IOException e) {
//            logger.error("Error writing error response", e);
//        }

    }

    @Override
    public void logout(HttpServletResponse request, HttpServletResponse response, Authentication authentication) {

    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {

    }


    private void sendValidationEmail(Account account) {
        String validatedToken = generateActiveToken(account);
        emailService.sendSimpleEmail(account.fullName(), account.getEmail(), validatedToken);
//        emailService.sendEmail(
//                account.getEmail(),
//                account.fullName(),
//                EmailTemplateName.ACTIVATE_ACCOUNT,
//                activationUrl,
//                validateToken,
//                "Account activation"
//        );

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
