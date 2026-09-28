package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.Assinatura;
import br.com.fiap.vaultix.domain.Cliente;
import br.com.fiap.vaultix.domain.PlanoAssinatura;
import br.com.fiap.vaultix.domain.enums.StatusAssinatura;
import br.com.fiap.vaultix.dto.AssinaturaRequest;
import br.com.fiap.vaultix.dto.AssinaturaResponse;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AssinaturaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssinaturaService {

    private final AssinaturaRepository repo;
    private final ClienteService clienteService;
    private final PlanoService planoService;

    public AssinaturaService(AssinaturaRepository repo,
                             ClienteService clienteService,
                             PlanoService planoService) {
        this.repo = repo;
        this.clienteService = clienteService;
        this.planoService = planoService;
    }

    @Transactional(readOnly = true)
    public Page<AssinaturaResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(AssinaturaResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AssinaturaResponse buscar(Long id) {
        return AssinaturaResponse.fromEntity(buscarEntidade(id));
    }

    private Assinatura buscarEntidade(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura", id));
    }

    @Transactional(readOnly = true)
    public List<AssinaturaResponse> listarPorCliente(Long idCliente) {
        clienteService.buscarEntidade(idCliente);
        return repo.findByClienteId(idCliente).stream()
                .map(AssinaturaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AssinaturaResponse criar(AssinaturaRequest req) {
        Cliente cliente = clienteService.buscarEntidade(req.idCliente());
        PlanoAssinatura plano = planoService.buscarEntidade(req.idPlano());

        Assinatura a = Assinatura.builder()
                .cliente(cliente)
                .plano(plano)
                .dtVencimento(req.dtVencimento())
                .status(req.status() != null ? req.status() : StatusAssinatura.ATIVA)
                .build();
        return AssinaturaResponse.fromEntity(repo.save(a));
    }

    @Transactional
    public AssinaturaResponse atualizar(Long id, AssinaturaRequest req) {
        Assinatura a = buscarEntidade(id);
        Cliente cliente = clienteService.buscarEntidade(req.idCliente());
        PlanoAssinatura plano = planoService.buscarEntidade(req.idPlano());

        a.setCliente(cliente);
        a.setPlano(plano);
        a.setDtVencimento(req.dtVencimento());
        if (req.status() != null) {
            a.setStatus(req.status());
        }
        return AssinaturaResponse.fromEntity(repo.save(a));
    }

    @Transactional
    public void remover(Long id) {
        Assinatura a = buscarEntidade(id);
        repo.delete(a);
    }
}
