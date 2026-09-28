package br.com.fiap.vaultix.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDITORIA_SEGURANCA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaSeguranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AUDITORIA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INCIDENTE")
    private Incidente incidente;

    @Column(name = "TIPO_ACAO", nullable = false, length = 100)
    private String tipoAcao;

    @Column(name = "DESCRICAO", length = 500)
    private String descricao;

    @CreationTimestamp
    @Column(name = "DT_REGISTRO", nullable = false, updatable = false)
    private LocalDateTime dtRegistro;

    @Column(name = "RESULTADO", length = 200)
    @Builder.Default
    private String resultado = "PENDENTE";
}
