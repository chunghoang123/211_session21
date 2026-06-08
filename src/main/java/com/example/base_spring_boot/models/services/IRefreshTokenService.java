package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.entities.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllByUserId(Long userId);
}
