package com.exe201.ilink.model.entity;

import com.exe201.ilink.model.enums.TokenType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "token", indexes = {
    @Index(name = "idx_access_token", columnList = "access_token"),
    @Index(name = "idx_refresh_token", columnList = "refresh_token"),
})
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("access_token")
    @Column(name = "access_token", length = 450)
    private String accessToken;

    @JsonProperty("refresh_token")
    @Column(name = "refresh_token", length = 350)
    private String refreshToken;

    @JsonProperty("token_type")
    @Column(name = "token_type")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private boolean expired;

    private boolean revoked;


}
