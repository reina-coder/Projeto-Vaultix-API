package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.enums.StatusIncidente;
import jakarta.validation.constraints.NotNull;

public record IncidenteStatusRequest(
        @NotNull(message = "status é obrigatório")
        StatusIncidente status
) {}
