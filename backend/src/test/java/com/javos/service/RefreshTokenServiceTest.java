/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.service;

import com.javos.exception.ResourceNotFoundException;
import com.javos.model.RefreshToken;
import com.javos.model.User;
import com.javos.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken revokedToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiryDays", 7L);

        user = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .name("John Doe")
                .build();

        validToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("valid-token-uuid")
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        expiredToken = RefreshToken.builder()
                .id(2L)
                .user(user)
                .token("expired-token-uuid")
                .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        revokedToken = RefreshToken.builder()
                .id(3L)
                .user(user)
                .token("revoked-token-uuid")
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(true)
                .build();
    }

    @Test
    void createRefreshToken_savesAndReturnsToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validToken);

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.isRevoked()).isFalse();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_tokenHasExpiryInFuture() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void findByToken_existingToken_returnsToken() {
        when(refreshTokenRepository.findByToken("valid-token-uuid")).thenReturn(Optional.of(validToken));

        RefreshToken result = refreshTokenService.findByToken("valid-token-uuid");

        assertThat(result.getToken()).isEqualTo("valid-token-uuid");
        assertThat(result.getUser().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByToken_nonExistingToken_throwsResourceNotFoundException() {
        when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findByToken("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void verifyExpiration_validToken_returnsToken() {
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        assertThat(result).isEqualTo(validToken);
    }

    @Test
    void verifyExpiration_revokedToken_throwsIllegalStateException() {
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(revokedToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("revogado");
    }

    @Test
    void verifyExpiration_expiredToken_revokesAndThrowsIllegalStateException() {
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expirado");

        verify(refreshTokenRepository).revokeByToken("expired-token-uuid");
    }

    @Test
    void revokeToken_callsRepositoryRevokeByToken() {
        refreshTokenService.revokeToken("valid-token-uuid");

        verify(refreshTokenRepository).revokeByToken("valid-token-uuid");
    }

    @Test
    void revokeAllUserTokens_callsRepositoryRevokeAllByUserId() {
        refreshTokenService.revokeAllUserTokens(1L);

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}
