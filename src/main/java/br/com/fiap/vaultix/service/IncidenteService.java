package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.Cliente;
import br.com.fiap.vaultix.domain.Incidente;
import br.com.fiap.vaultix.domain.enums.Severidade;
import br.com.fiap.vaultix.domain.enums.StatusIncidente;
import br.com.fiap.vaultix.dto.IncidenteRequest;
import br.com.fiap.vaultix.dto.IncidenteResponse;
import br.com.fiap.vaultix.dto.IncidenteStatusRequest;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AuditoriaRepository;
import br.com.fiap.vaultix.repository.IncidenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidenteService {

    private final IncidenteRepository repo;
    private final ClienteService clienteService;
    private final AuditoriaRepository auditoriaRepo;

    public IncidenteService(IncidenteRepository repo,
                            ClienteService clienteService,
                            AuditoriaRepository auditoriaRepo) {
        this.repo = repo;
        this.clienteService = clienteService;
        this.auditoriaRepo = auditoriaRepo;
    }

    @Transactional(readOnly = true)
    public Page<IncidenteResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(IncidenteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public IncidenteResponse buscar(Long id) {
        return IncidenteResponse.fromEntity(buscarEntidade(id));
    }

    private Incidente buscarEntidade(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", id));
    }

    @Transactional(readOnly = true)
    public List<IncidenteResponse> historicoPorCliente(Long idCliente) {
        clienteService.buscarEntidade(idCliente);
        return repo.findByClienteId(idCliente).stream()
                .map(IncidenteResponse::fromEntity)
                .toList();
    }

    @Transactional
    public IncidenteResponse criar(IncidenteRequest req) {
        Cliente cliente = clienteService.buscarEntidade(req.idCliente());

        Incidente i = Incidente.builder()
                .cliente(cliente)
                .tipoIncidente(req.tipoIncidente())
                .descricao(req.descricao())
                .severidade(req.severidade() != null ? req.severidade() : Severidade.MEDIA)
                .status(req.status() != null ? req.status() : StatusIncidente.ABERTO)
                .build();

        // ao salvar, as triggers TRG_AUDIT_NOVO_INCIDENTE e TRG_ESCALONA_STATUS_CRITICO disparam no Oracle
        Incidente salvo = repo.save(i);
        // refresca pra pegar o status atualizado pela trigger (CRITICA -> EM_ATENDIMENTO)
        return IncidenteResponse.fromEntity(repo.findById(salvo.getId()).orElseThrow());
    }

    @Transactional
    public IncidenteResponse atualizar(Long id, IncidenteRequest req) {
        Incidente i = buscarEntidade(id);
        Cliente cliente = clienteService.buscarEntidade(req.idCliente());

        i.setCliente(cliente);
        i.setTipoIncidente(req.tipoIncidente());
        i.setDescricao(req.descricao());
        if (req.severidade() != null) i.setSeveridade(req.severidade());
        if (req.status() != null) i.setStatus(req.status());

        return IncidenteResponse.fromEntity(repo.save(i));
    }

    @Transactional
    public IncidenteResponse atualizarStatus(Long id, IncidenteStatusRequest req) {
        Incidente i = buscarEntidade(id);
        i.setStatus(req.status());
        Incidente salvo = repo.save(i);
        // trigger TRG_AUDIT_RESOLUCAO_INCIDENTE dispara automaticamente quando STATUS -> RESOLVIDO
        return IncidenteResponse.fromEntity(salvo);
    }

    @Transactional
    public void remover(Long id) {
        Incidente i = buscarEntidade(id);

        // AUDITORIA_SEGURANCA referencia INCIDENTE via FK_AUD_INC.
        // As auditorias geradas pelas triggers (TRG_AUDIT_NOVO_INCIDENTE,
        // TRG_AUDIT_RESOLUCAO_INCIDENTE, ESCALONAMENTO_CRITICO) pertencem
        // intrinsecamente ao incidente; ao apagá-lo, removemos sua trilha
        // junto, evitando ORA-02292.
        auditoriaRepo.deleteAll(auditoriaRepo.findByIncidenteId(id));

        repo.delete(i);
    }
}
