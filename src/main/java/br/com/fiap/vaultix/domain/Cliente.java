package br.com.fiap.vaultix.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "CLIENTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "CPF", nullable = false, unique = true, length = 11, columnDefinition = "CHAR(11)")
    private String cpf;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @CreationTimestamp
    @Column(name = "DT_CADASTRO", nullable = false, updatable = false)
    private LocalDate dtCadastro;
}
