package br.com.fiap.vaultix.domain;

import br.com.fiap.vaultix.domain.enums.StatusAssinatura;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "ASSINATURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASSINATURA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO", nullable = false)
    private PlanoAssinatura plano;

    @CreationTimestamp
    @Column(name = "DT_INICIO", nullable = false, updatable = false)
    private LocalDate dtInicio;

    @Column(name = "DT_VENCIMENTO", nullable = false)
    private LocalDate dtVencimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private StatusAssinatura status = StatusAssinatura.ATIVA;
}
