package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.Incidente;
import br.com.fiap.vaultix.domain.enums.Severidade;
import br.com.fiap.vaultix.domain.enums.StatusIncidente;
import br.com.fiap.vaultix.domain.enums.TipoIncidente;

import java.time.LocalDateTime;

public record IncidenteResponse(
        Long id,
        Long idCliente,
        String nomeCliente,
        TipoIncidente tipoIncidente,
        String descricao,
        LocalDateTime dtOcorrencia,
        Severidade severidade,
        StatusIncidente status
) {
    public static IncidenteResponse fromEntity(Incidente i) {
        return new IncidenteResponse(
                i.getId(),
                i.getCliente().getId(),
                i.getCliente().getNome(),
                i.getTipoIncidente(),
                i.getDescricao(),
                i.getDtOcorrencia(),
                i.getSeveridade(),
                i.getStatus()
        );
    }
}
