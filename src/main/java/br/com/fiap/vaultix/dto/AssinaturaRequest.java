package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.enums.StatusAssinatura;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AssinaturaRequest(

        @NotNull(message = "idCliente é obrigatório")
        Long idCliente,

        @NotNull(message = "idPlano é obrigatório")
        Long idPlano,

        @NotNull(message = "data de vencimento é obrigatória")
        @Future(message = "data de vencimento deve ser futura")
        LocalDate dtVencimento,

        StatusAssinatura status
) {}
