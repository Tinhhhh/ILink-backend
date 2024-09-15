package com.exe201.ilink.sercurity;

import com.exe201.ilink.model.entity.Token;
import com.exe201.ilink.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomeLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomeLogoutHandler.class);

    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain");
            try {
                response.getWriter().write("No JWT token found in the request header");
            } catch (IOException e) {
                logger.error("Error writing unauthorized response", e);
            }

            return;
        }

        jwt = authHeader.substring(7);
        Token token = tokenRepository.findTokenByAccessToken(jwt);

    }
}
