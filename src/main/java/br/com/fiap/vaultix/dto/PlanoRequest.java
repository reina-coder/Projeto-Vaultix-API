package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.enums.NivelPlano;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanoRequest(
        @NotBlank(message = "nome do plano é obrigatório")
        @Size(max = 50)
        String nome,

        @NotNull(message = "valor mensal é obrigatório")
        @DecimalMin(value = "0.01", message = "valor mensal deve ser maior que zero")
        BigDecimal valorMensal,

        @Size(max = 300)
        String descricao,

        @NotNull(message = "nível é obrigatório (BASICO, PREMIUM ou ENTERPRISE)")
        NivelPlano nivel
) {}
