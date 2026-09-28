package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.dto.AuditoriaResponse;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repo;
    private final ClienteService clienteService;

    public AuditoriaService(AuditoriaRepository repo, ClienteService clienteService) {
        this.repo = repo;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(AuditoriaResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AuditoriaResponse buscar(Long id) {
        return AuditoriaResponse.fromEntity(
                repo.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Auditoria", id))
        );
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarPorCliente(Long idCliente) {
        clienteService.buscarEntidade(idCliente);
        return repo.findByClienteId(idCliente).stream()
                .map(AuditoriaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarPorIncidente(Long idIncidente) {
        return repo.findByIncidenteId(idIncidente).stream()
                .map(AuditoriaResponse::fromEntity)
                .toList();
    }
}
