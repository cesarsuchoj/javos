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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

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

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 8, max = 128, message = "Password deve ter entre 8 e 128 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password deve conter ao menos uma letra minúscula, uma maiúscula e um número"
    )
    private String password;
}
