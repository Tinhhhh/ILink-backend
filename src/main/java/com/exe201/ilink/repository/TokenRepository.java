package com.exe201.ilink.repository;


import com.exe201.ilink.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("SELECT t FROM Token t WHERE t.account.accountId = :accountId AND t.expired = false AND t.revoked = false")
    List<Token> findAllValidTokensByUser(UUID accountId);

    Token findTokenByAccessToken(String AccessToken);

    Optional<Token> findByRefreshTokenAndRevokedFalseAndExpiredFalse(String refreshToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.revoked = true AND t.expired = true")
    void deleteTokensByRevokedTrueAndExpiredTrue();
}
