package br.com.fiap.vaultix.repository;

import br.com.fiap.vaultix.domain.Incidente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {
    List<Incidente> findByClienteId(Long idCliente);
}
