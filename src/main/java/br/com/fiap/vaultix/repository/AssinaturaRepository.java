package br.com.fiap.vaultix.repository;

import br.com.fiap.vaultix.domain.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    List<Assinatura> findByClienteId(Long idCliente);
    boolean existsByPlanoId(Long idPlano);
}