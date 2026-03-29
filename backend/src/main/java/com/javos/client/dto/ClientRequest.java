package com.javos.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank @Size(max = 150)
    private String name;
    @Email @Size(max = 100)
    private String email;
    @Pattern(regexp = "^[+\\d\\s()./-]{0,20}$", message = "Telefone inválido")
    @Size(max = 20)
    private String phone;
    @Size(max = 20)
    private String document;
    @Size(max = 255)
    private String address;
    @Size(max = 100)
    private String city;
    @Size(max = 2)
    private String state;
    @Pattern(regexp = "^\\d{5}-?\\d{3}$|^$", message = "CEP inválido")
    @Size(max = 10)
    private String zipCode;
    private boolean active = true;
    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
