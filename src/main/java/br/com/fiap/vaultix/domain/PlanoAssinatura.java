package br.com.fiap.vaultix.domain;

import br.com.fiap.vaultix.domain.enums.NivelPlano;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PLANO_ASSINATURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAssinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANO")
    private Long id;

    @Column(name = "NOME_PLANO", nullable = false, length = 50)
    private String nome;

    @Column(name = "VALOR_MENSAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensal;

    @Column(name = "DESCRICAO", length = 300)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "NIVEL", nullable = false, length = 20)
    private NivelPlano nivel;
}
