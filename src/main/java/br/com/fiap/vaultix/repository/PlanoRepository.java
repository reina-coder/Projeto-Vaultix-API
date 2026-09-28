package br.com.fiap.vaultix.repository;

import br.com.fiap.vaultix.domain.PlanoAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<PlanoAssinatura, Long> {
}
