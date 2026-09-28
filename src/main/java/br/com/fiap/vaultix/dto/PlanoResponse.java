package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.PlanoAssinatura;
import br.com.fiap.vaultix.domain.enums.NivelPlano;

import java.math.BigDecimal;

public record PlanoResponse(
        Long id,
        String nome,
        BigDecimal valorMensal,
        String descricao,
        NivelPlano nivel
) {
    public static PlanoResponse fromEntity(PlanoAssinatura p) {
        return new PlanoResponse(
                p.getId(),
                p.getNome(),
                p.getValorMensal(),
                p.getDescricao(),
                p.getNivel()
        );
    }
}
