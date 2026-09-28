package br.com.fiap.vaultix.repository;

import br.com.fiap.vaultix.domain.AuditoriaSeguranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<AuditoriaSeguranca, Long> {
    List<AuditoriaSeguranca> findByClienteId(Long idCliente);
    List<AuditoriaSeguranca> findByIncidenteId(Long idIncidente);
}
