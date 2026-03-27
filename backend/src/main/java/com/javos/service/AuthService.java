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

import com.javos.config.JwtTokenProvider;
import com.javos.dto.LoginRequest;
import com.javos.dto.LoginResponse;
import com.javos.dto.RefreshTokenRequest;
import com.javos.dto.RegisterRequest;
import com.javos.dto.UserDTO;
import com.javos.exception.ResourceAlreadyExistsException;
import com.javos.model.RefreshToken;
import com.javos.model.RevokedToken;
import com.javos.model.Role;
import com.javos.model.User;
import com.javos.repository.RevokedTokenRepository;
import com.javos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${javos.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .expiresIn(jwtExpirationMs / 1000)
                .build();
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(existing);

        User user = existing.getUser();

        // Token rotation: revoke old refresh token, issue new one
        refreshTokenService.revokeToken(existing.getToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .type("Bearer")
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .expiresIn(jwtExpirationMs / 1000)
                .build();
    }

    @Transactional
    public void logout(String refreshToken, String bearerAccessToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenService.revokeToken(refreshToken);
        }
        if (StringUtils.hasText(bearerAccessToken) && bearerAccessToken.startsWith("Bearer ")) {
            String accessToken = bearerAccessToken.substring(7);
            if (jwtTokenProvider.validateToken(accessToken)) {
                String jti = jwtTokenProvider.getJtiFromToken(accessToken);
                if (!revokedTokenRepository.existsByJti(jti)) {
                    revokedTokenRepository.save(RevokedToken.builder()
                            .jti(jti)
                            .username(jwtTokenProvider.getUsernameFromToken(accessToken))
                            .expiryDate(jwtTokenProvider.getExpirationFromToken(accessToken))
                            .build());
                }
            }
        }
    }

    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username já está em uso: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email já está em uso: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
