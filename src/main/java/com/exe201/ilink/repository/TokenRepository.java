package com.exe201.ilink.repository;


import com.exe201.ilink.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findTokenByAccessToken(String AccessToken);

    Token findByRefreshTokenAndRevokedFalseAndExpiredFalse(String refreshToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.revoked = true AND t.expired = true")
    void deleteTokensByRevokedTrueAndExpiredTrue();
}
