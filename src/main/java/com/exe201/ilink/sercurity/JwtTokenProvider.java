package com.exe201.ilink.sercurity;

import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.repository.ResetPasswordTokenRepository;
import com.exe201.ilink.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final TokenRepository tokenRepository;
    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiration.access-token}")
    private Long accessTokenExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private Long refreshTokenExpiration;

    public JwtTokenProvider(ResetPasswordTokenRepository resetPasswordTokenRepository, TokenRepository tokenRepository) {
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.tokenRepository = tokenRepository;
    }


    // generate accessToken
    public String generateAccessToken(Authentication authentication) {
        String token = generateToken(authentication, accessTokenExpiration);
        return token;
    }

    // generate refreshToken
    public String generateRefreshToken(Authentication authentication) {
        String token = generateToken(authentication, refreshTokenExpiration);
        return token;
    }

    // generate JWT token
    public String generateToken(Authentication authentication, Long expiration) {
        String username = authentication.getName();

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key())
                .compact();
        return token;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Get username from token
     * @param token
     * @return userEmail
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        return username;
    }

    // validate JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new ILinkException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new ILinkException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new ILinkException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            throw new ILinkException("JWT claims string is empty");
        }
    }

    public boolean isTokenValid(String token, String checkUsername) {
        final String username = getUsername(token);
        return (username.equals(checkUsername) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date(System.currentTimeMillis()));
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDeleteExpiredToken() {
        tokenRepository.deleteTokensByRevokedTrueAndExpiredTrue();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDeleteExpiredResetPasswordToken() {
        resetPasswordTokenRepository.deleteTokensByRevokedTrue();
    }
}
