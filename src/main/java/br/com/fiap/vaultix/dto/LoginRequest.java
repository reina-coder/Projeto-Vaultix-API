package br.com.fiap.vaultix.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username é obrigatório")
        String username,
        @NotBlank(message = "password é obrigatória")
        String password
) {}
