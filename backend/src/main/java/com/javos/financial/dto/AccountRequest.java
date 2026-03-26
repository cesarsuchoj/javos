package com.javos.financial.dto;

import com.javos.financial.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @NotNull
    private AccountType type;
    private BigDecimal balance;
    private boolean active = true;
}
