package com.exe201.ilink.repository;

import com.exe201.ilink.model.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.isRevoked")
    void deleteTokensByRevokedTrue();

    Optional<PasswordResetToken> findByToken(String token);
}
