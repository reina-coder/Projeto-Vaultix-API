package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.Cliente;
import br.com.fiap.vaultix.dto.ClienteRequest;
import br.com.fiap.vaultix.dto.ClienteResponse;
import br.com.fiap.vaultix.exception.BusinessException;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AssinaturaRepository;
import br.com.fiap.vaultix.repository.AuditoriaRepository;
import br.com.fiap.vaultix.repository.ClienteRepository;
import br.com.fiap.vaultix.repository.IncidenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final AssinaturaRepository assinaturaRepo;
    private final IncidenteRepository incidenteRepo;
    private final AuditoriaRepository auditoriaRepo;

    public ClienteService(ClienteRepository repo,
                          AssinaturaRepository assinaturaRepo,
                          IncidenteRepository incidenteRepo,
                          AuditoriaRepository auditoriaRepo) {
        this.repo = repo;
        this.assinaturaRepo = assinaturaRepo;
        this.incidenteRepo = incidenteRepo;
        this.auditoriaRepo = auditoriaRepo;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(ClienteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.fromEntity(buscarEntidade(id));
    }

    public Cliente buscarEntidade(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest req) {
        if (repo.existsByCpf(req.cpf())) {
            throw new BusinessException("Já existe cliente cadastrado com este CPF.");
        }
        if (repo.existsByEmail(req.email())) {
            throw new BusinessException("Já existe cliente cadastrado com este e-mail.");
        }
        Cliente c = Cliente.builder()
                .nome(req.nome())
                .email(req.email())
                .cpf(req.cpf())
                .telefone(req.telefone())
                .build();
        return ClienteResponse.fromEntity(repo.save(c));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest req) {
        Cliente c = buscarEntidade(id);
        if (!c.getCpf().equals(req.cpf()) && repo.existsByCpf(req.cpf())) {
            throw new BusinessException("Já existe outro cliente com este CPF.");
        }
        if (!c.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new BusinessException("Já existe outro cliente com este e-mail.");
        }
        c.setNome(req.nome());
        c.setEmail(req.email());
        c.setCpf(req.cpf());
        c.setTelefone(req.telefone());
        return ClienteResponse.fromEntity(repo.save(c));
    }

    @Transactional
    public void remover(Long id) {
        Cliente c = buscarEntidade(id);

        // 1) AUDITORIA_SEGURANCA depende de CLIENTE (FK_AUD_CLI) e de INCIDENTE (FK_AUD_INC).
        //    Remover primeiro as auditorias do próprio cliente...
        auditoriaRepo.deleteAll(auditoriaRepo.findByClienteId(id));

        // ...e também qualquer auditoria que aponte para incidentes deste cliente,
        //    cobrindo o caso (permitido pelo schema) de auditoria com ID_CLIENTE de outro
        //    cliente porém ID_INCIDENTE pertencente a este.
        incidenteRepo.findByClienteId(id).forEach(inc ->
                auditoriaRepo.deleteAll(auditoriaRepo.findByIncidenteId(inc.getId()))
        );

        // 2) INCIDENTE depende de CLIENTE (FK_INC_CLI)
        incidenteRepo.deleteAll(incidenteRepo.findByClienteId(id));

        // 3) ASSINATURA depende de CLIENTE (FK_ASIN_CLI)
        assinaturaRepo.deleteAll(assinaturaRepo.findByClienteId(id));

        // 4) Agora o CLIENTE pode ser removido sem violar ORA-02292
        repo.delete(c);
    }
}
