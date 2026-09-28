package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.PlanoAssinatura;
import br.com.fiap.vaultix.dto.PlanoRequest;
import br.com.fiap.vaultix.dto.PlanoResponse;
import br.com.fiap.vaultix.exception.BusinessException;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AssinaturaRepository;
import br.com.fiap.vaultix.repository.PlanoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanoService {

    private final PlanoRepository repo;
    private final AssinaturaRepository assinaturaRepo;

    public PlanoService(PlanoRepository repo, AssinaturaRepository assinaturaRepo) {
        this.repo = repo;
        this.assinaturaRepo = assinaturaRepo;
    }

    @Transactional(readOnly = true)
    public Page<PlanoResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(PlanoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public PlanoResponse buscar(Long id) {
        return PlanoResponse.fromEntity(buscarEntidade(id));
    }

    public PlanoAssinatura buscarEntidade(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano", id));
    }

    @Transactional
    public PlanoResponse criar(PlanoRequest req) {
        PlanoAssinatura p = PlanoAssinatura.builder()
                .nome(req.nome())
                .valorMensal(req.valorMensal())
                .descricao(req.descricao())
                .nivel(req.nivel())
                .build();
        return PlanoResponse.fromEntity(repo.save(p));
    }

    @Transactional
    public PlanoResponse atualizar(Long id, PlanoRequest req) {
        PlanoAssinatura p = buscarEntidade(id);
        p.setNome(req.nome());
        p.setValorMensal(req.valorMensal());
        p.setDescricao(req.descricao());
        p.setNivel(req.nivel());
        return PlanoResponse.fromEntity(repo.save(p));
    }

    @Transactional
    public void remover(Long id) {
        PlanoAssinatura p = buscarEntidade(id);

        // PLANO_ASSINATURA é referenciada por ASSINATURA (FK_ASIN_PLANO).
        // Apagar um plano com assinaturas vinculadas violaria ORA-02292 e,
        // sobretudo, destruiria/orfanizaria dados de clientes pagantes.
        // Bloqueamos a operação com mensagem de negócio (422).
        if (assinaturaRepo.existsByPlanoId(id)) {
            throw new BusinessException(
                    "Não é possível remover este plano: existem assinaturas vinculadas. " +
                            "Cancele ou migre as assinaturas antes de remover o plano."
            );
        }

        repo.delete(p);
    }
}
