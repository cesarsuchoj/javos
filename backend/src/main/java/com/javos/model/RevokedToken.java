/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "revoked_access_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(updatable = false)
    @Builder.Default
    private Instant revokedAt = Instant.now();
}
