package br.com.fiap.vaultix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(min = 3, max = 100, message = "username deve ter entre 3 e 100 caracteres")
        String username,

        @NotBlank(message = "password é obrigatória")
        @Size(min = 6, max = 100, message = "password deve ter no mínimo 6 caracteres")
        String password
) {}
