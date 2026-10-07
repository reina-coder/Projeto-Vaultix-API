package br.com.fiap.vaultix.service;

import br.com.fiap.vaultix.domain.PlanoAssinatura;
import br.com.fiap.vaultix.domain.enums.NivelPlano;
import br.com.fiap.vaultix.dto.PlanoRequest;
import br.com.fiap.vaultix.dto.PlanoResponse;
import br.com.fiap.vaultix.exception.BusinessException;
import br.com.fiap.vaultix.exception.ResourceNotFoundException;
import br.com.fiap.vaultix.repository.AssinaturaRepository;
import br.com.fiap.vaultix.repository.PlanoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @Mock
    private PlanoRepository repo;

    @Mock
    private AssinaturaRepository assinaturaRepo;

    @InjectMocks
    private PlanoService service;

    private PlanoAssinatura plano() {
        return PlanoAssinatura.builder()
                .id(1L)
                .nome("Premium")
                .valorMensal(new BigDecimal("49.90"))
                .descricao("Plano Premium")
                .nivel(NivelPlano.PREMIUM)
                .build();
    }

    @Test
    void naoDeveRemoverPlanoComAssinaturas() {
        when(repo.findById(1L)).thenReturn(Optional.of(plano()));
        when(assinaturaRepo.existsByPlanoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existem assinaturas vinculadas");

        verify(repo, never()).delete(any());
    }

    @Test
    void deveRemoverPlanoSemAssinaturas() {
        PlanoAssinatura plano = plano();

        when(repo.findById(1L)).thenReturn(Optional.of(plano));
        when(assinaturaRepo.existsByPlanoId(1L)).thenReturn(false);

        service.remover(1L);

        verify(repo).delete(plano);
    }

    @Test
    void deveLancarExcecaoAoBuscarPlanoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarEntidade(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repo).findById(99L);
        verifyNoInteractions(assinaturaRepo);
    }

    @Test
    void deveCriarPlanoComDadosRecebidos() {
        PlanoRequest request = new PlanoRequest(
                "Premium",
                new BigDecimal("49.90"),
                "Plano Premium",
                NivelPlano.PREMIUM
        );

        PlanoAssinatura salvo = plano();
        when(repo.save(any(PlanoAssinatura.class))).thenReturn(salvo);

        PlanoResponse response = service.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Premium");
        assertThat(response.valorMensal()).isEqualByComparingTo("49.90");
        assertThat(response.nivel()).isEqualTo(NivelPlano.PREMIUM);

        verify(repo).save(any(PlanoAssinatura.class));
    }
}
