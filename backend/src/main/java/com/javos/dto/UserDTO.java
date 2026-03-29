/*
 * Javos - Sistema de gestão inspirado no MAP-OS
 * Copyright (C) 2024 Javos Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.javos.dto;

import com.javos.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username deve conter apenas letras, números, '.', '_' ou '-'")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(min = 8, max = 128, message = "Password deve ter entre 8 e 128 caracteres")
    private String password;

    private Role role;

    private boolean active;

    private LocalDateTime createdAt;
}
