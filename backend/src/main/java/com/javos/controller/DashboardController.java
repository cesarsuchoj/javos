/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.controller;

import com.javos.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Resumo geral do sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final UserRepository userRepository;

    @GetMapping("/summary")
    @Operation(summary = "Resumo do dashboard", description = "Retorna contadores e informações gerais do sistema para o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<Map<String, Object>> getSummary(Authentication authentication) {
        long totalUsers = userRepository.count();
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "loggedUser", authentication.getName(),
                "version", "0.0.1-SNAPSHOT"
        ));
    }
}
