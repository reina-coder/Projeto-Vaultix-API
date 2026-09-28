package br.com.fiap.vaultix.domain;

import br.com.fiap.vaultix.domain.enums.Severidade;
import br.com.fiap.vaultix.domain.enums.StatusIncidente;
import br.com.fiap.vaultix.domain.enums.TipoIncidente;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "INCIDENTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INCIDENTE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_INCIDENTE", nullable = false, length = 50)
    private TipoIncidente tipoIncidente;

    @Column(name = "DESCRICAO", length = 500)
    private String descricao;

    @CreationTimestamp
    @Column(name = "DT_OCORRENCIA", nullable = false, updatable = false)
    private LocalDateTime dtOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERIDADE", nullable = false, length = 20)
    @Builder.Default
    private Severidade severidade = Severidade.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 25)
    @Builder.Default
    private StatusIncidente status = StatusIncidente.ABERTO;
}
