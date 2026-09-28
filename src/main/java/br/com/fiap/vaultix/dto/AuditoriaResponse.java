package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.AuditoriaSeguranca;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        Long idCliente,
        Long idIncidente,
        String tipoAcao,
        String descricao,
        LocalDateTime dtRegistro,
        String resultado
) {
    public static AuditoriaResponse fromEntity(AuditoriaSeguranca a) {
        return new AuditoriaResponse(
                a.getId(),
                a.getCliente().getId(),
                a.getIncidente() != null ? a.getIncidente().getId() : null,
                a.getTipoAcao(),
                a.getDescricao(),
                a.getDtRegistro(),
                a.getResultado()
        );
    }
}
