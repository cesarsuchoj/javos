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

import com.javos.dto.UserDTO;
import com.javos.exception.ResourceNotFoundException;
import com.javos.model.Role;
import com.javos.model.User;
import com.javos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .name("John Doe")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .name("John Doe")
                .role(Role.ROLE_USER)
                .active(true)
                .build();
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDTO> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsUserDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void update_existingUser_updatesNameEmailRoleActive() {
        UserDTO dto = UserDTO.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .role(Role.ROLE_ADMIN)
                .active(false)
                .build();

        User updated = User.builder()
                .id(1L)
                .username("johndoe")
                .email("jane@example.com")
                .name("Jane Doe")
                .role(Role.ROLE_ADMIN)
                .active(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserDTO result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(result.isActive()).isFalse();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_withNewPassword_encodesPassword() {
        UserDTO dto = UserDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("newpassword123")
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.update(1L, dto);

        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(argThat(u -> "newEncodedPassword".equals(u.getPassword())));
    }

    @Test
    void update_withBlankPassword_doesNotEncodeOrChangePassword() {
        UserDTO dto = UserDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("")
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.update(1L, dto);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_withNullPassword_doesNotChangePassword() {
        UserDTO dto = UserDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .password(null)
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.update(1L, dto);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999L, userDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void delete_existingUser_deletesById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void findById_responseDoesNotIncludePassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.findById(1L);

        assertThat(result.getPassword()).isNull();
    }
}
