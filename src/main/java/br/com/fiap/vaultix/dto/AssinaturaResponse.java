package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.Assinatura;
import br.com.fiap.vaultix.domain.enums.StatusAssinatura;

import java.time.LocalDate;

public record AssinaturaResponse(
        Long id,
        Long idCliente,
        String nomeCliente,
        Long idPlano,
        String nomePlano,
        LocalDate dtInicio,
        LocalDate dtVencimento,
        StatusAssinatura status
) {
    public static AssinaturaResponse fromEntity(Assinatura a) {
        return new AssinaturaResponse(
                a.getId(),
                a.getCliente().getId(),
                a.getCliente().getNome(),
                a.getPlano().getId(),
                a.getPlano().getNome(),
                a.getDtInicio(),
                a.getDtVencimento(),
                a.getStatus()
        );
    }
}
