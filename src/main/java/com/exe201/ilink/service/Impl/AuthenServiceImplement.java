package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.enums.TokenType;
import com.exe201.ilink.model.exception.ActivationCodeException;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.exception.RegisterAccountExistedException;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.EmailToken;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.entity.Token;
import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.model.payload.dto.response.AuthenticationResponse;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.EmailTokenRepository;
import com.exe201.ilink.repository.RoleRepository;
import com.exe201.ilink.repository.TokenRepository;
import com.exe201.ilink.sercurity.CustomUserDetailsService;
import com.exe201.ilink.sercurity.JwtTokenProvider;
import com.exe201.ilink.service.AuthenService;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final CustomUserDetailsService userDetailsService;

    @Value("${application.email.secure.characters}")
    private String emailSecureChar;

//    private static final Logger logger = LoggerFactory.getLogger(LogoutServiceConfig.class);

    @Override
    public void register(RegistrationRequest request) throws MessagingException {
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ILinkException("Authentication fails. Your authentication information is incorrect, please try again"));
        if (!account.isEnabled()) {
            throw new ILinkException("Account is not Enabled. Please use the last activation code sent to your email to activate your account");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        revokeAllUserToken(account);
        saveUserToken(account, accessToken, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void activeAccount(String token, HttpServletResponse response) throws MessagingException {

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

        Account account = accountRepository.findById(savedCode.getAccount().getAccountId())
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
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwtToken;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ILinkException("No JWT token found in the request header");
        }

        jwtToken = authHeader.substring(7);
        Token token = tokenRepository.findByRefreshTokenAndRevokedFalseAndExpiredFalse(jwtToken)
                .orElseThrow(() -> new ILinkException("Invalid access token. Access token is revoked or expired"));

        if (token!=null){
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("text/plain");
        }


    }

    @Override
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtTokenProvider.getUsername(refreshToken);

        if (userEmail != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            Token token = tokenRepository.findByRefreshTokenAndRevokedFalseAndExpiredFalse(refreshToken)
                    .orElseThrow(() -> new ILinkException("Invalid refresh token. Refresh token is revoked or expired"));
            if (!token.isRevoked() && !token.isExpired()) {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                Account account = accountRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new ILinkException("Invalid user. User not found"));

                revokeAllUserToken(account);

                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

                saveUserToken(account, accessToken, newRefreshToken);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .build();

            } else {
                throw new ILinkException("Token is invalid or not exist");
            }
        }

        return null;
    }


    private void sendValidationEmail(Account account) throws MessagingException {
        String validatedToken = generateActiveToken(account);
        emailService.sendMimeMessageWithHtml(account.fullName(), account.getEmail(), validatedToken);

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

    private void revokeAllUserToken(Account account) {
        var validUserToken = tokenRepository.findAllValidTokensByUser(account.getAccountId());
        if (validUserToken.isEmpty())
            return;
        validUserToken.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserToken);
    }

    private void saveUserToken(Account account, String jwtAccessToken, String jwtRefreshToken) {
        var token = Token.builder()
                .account(account)
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }


}
