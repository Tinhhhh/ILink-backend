package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.EmailTemplateName;
import com.exe201.ilink.model.enums.TokenType;
import com.exe201.ilink.model.exception.ActivationCodeException;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.exception.RegisterAccountExistedException;
import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.model.payload.dto.response.AuthenticationResponse;
import com.exe201.ilink.repository.*;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

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
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final ShopRepository shopRepository;

    @Value("${application.email.url}")
    private String url;

    @Value("${application.email.secure.characters}")
    private String emailSecureChar;

//    private static final Logger logger = LoggerFactory.getLogger(LogoutServiceConfig.class);

    @Override
    public void register(RegistrationRequest request) throws MessagingException {

        if (request.getRole().equals("BUYER")) {
            accountRegisteration(request);
        } if (request.getRole().equals("SELLER")) {
            shopRegisterion(request);
        } else {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Register request fails. Role not found");
        }

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
            .orElseThrow(() -> new ILinkException(HttpStatus.UNAUTHORIZED, "Authentication fails. Your authentication information is incorrect, please try again"));
        if (!account.isEnabled()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Account is not Enabled. Please use the last activation code sent to your email to activate your account");
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

    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwtToken;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ILinkException(HttpStatus.UNAUTHORIZED, "No JWT token found in the request header");
        }

        jwtToken = authHeader.substring(7);
        Token token = tokenRepository.findByAccessTokenAndRevokedFalseAndExpiredFalse(jwtToken).orElse(null);
        if (token != null) {
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
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
                .orElseThrow(() -> new ILinkException(HttpStatus.UNAUTHORIZED, "Invalid refresh token. Refresh token is revoked or expired"));
            if (!token.isRevoked() && !token.isExpired()) {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

                Account account = accountRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Invalid user. User not found"));

                revokeAllUserToken(account);

                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

                saveUserToken(account, accessToken, newRefreshToken);

                return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            } else {
                throw new ILinkException(HttpStatus.BAD_REQUEST, "Token is invalid or not exist");
            }
        }

        return null;
    }

    @Override
    public void forgotPassword(String email) throws NoSuchAlgorithmException, MessagingException {

        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        String token = generateResetPasswordToken(32);
        StringBuilder link = new StringBuilder();
        link.append(url).append("/auth/reset-password?").append("token=").append(token);
        emailService.sendMimeMessageWithHtml(
            account.fullName(), account.getEmail(), link.toString(),
            EmailTemplateName.FORGOT_PASSWORD.getName(), "Reset your password");

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
            .token(token)
            .expiryDate(LocalDateTime.now().plusMinutes(30))
            .isRevoked(false)
            .account(account)
            .build();

        resetPasswordTokenRepository.save(passwordResetToken);

    }

    @Override
    public void resetPassword(String newPassword, String token) {
        PasswordResetToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Token not found"));

        if (resetPasswordToken.isRevoked()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Token is invalid or revoked");
        }

        if (LocalDateTime.now().isAfter(resetPasswordToken.getExpiryDate())) {
            resetPasswordToken.setRevoked(true);
            resetPasswordTokenRepository.save(resetPasswordToken);
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Token is expired");
        }

        Account account = accountRepository.findById(resetPasswordToken.getAccount().getAccountId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Account not found"));

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        resetPasswordToken.setRevoked(true);
        resetPasswordTokenRepository.save(resetPasswordToken);

    }

    private void sendValidationEmail(Account account) throws MessagingException {
        String validatedToken = generateActiveToken(account);
        emailService.sendMimeMessageWithHtml(
            account.fullName(), account.getEmail(), validatedToken,
            EmailTemplateName.ACTIVATE_ACCOUNT.getName(), "Activate your new account");

    }

    private void sendInformEmailToShopOwner(Account account) throws MessagingException {
        emailService.sendMimeMessageWithHtml(
            account.fullName(), account.getEmail(), "",
            EmailTemplateName.INFORM_SHOP_OWNER.getName(), "Souvi has received your registration request");
    }


    private String generateResetPasswordToken(int codelength) throws NoSuchAlgorithmException {
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        //lay ngau nhien 1 ki tu trong chuoi emailSecurecHar
        for (int i = 0; i < codelength; i++) {
            int randomIndex = random.nextInt(emailSecureChar.length());
            codeBuilder.append(emailSecureChar.charAt(randomIndex));
        }

        //Hash token with SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeBuilder.toString().getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String generateActiveToken(Account account) {
        StringBuilder codeBuilder = new StringBuilder();
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

    private void shopRegisterion(RegistrationRequest request) throws MessagingException {
        Role accountRole = roleRepository.findByRoleName("SELLER")
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Role SELLER not found"));
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

        Shop shop = Shop.builder()
            .shopName(request.getShopName())
            .address(request.getShopAddress())
            .account(account)
            .build();
        shopRepository.save(shop);

        sendInformEmailToShopOwner(account);
    }

    private void accountRegisteration(RegistrationRequest request) throws MessagingException {
        Role accountRole = roleRepository.findByRoleName("BUYER")
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Role BUYER not found"));
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


}
