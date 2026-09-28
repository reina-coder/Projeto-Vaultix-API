package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.enums.Severidade;
import br.com.fiap.vaultix.domain.enums.StatusIncidente;
import br.com.fiap.vaultix.domain.enums.TipoIncidente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidenteRequest(

        @NotNull(message = "idCliente é obrigatório")
        Long idCliente,

        @NotNull(message = "tipo do incidente é obrigatório")
        TipoIncidente tipoIncidente,

        @Size(max = 500)
        String descricao,

        Severidade severidade,

        StatusIncidente status
) {}
