package br.com.fiap.vaultix.dto;

import br.com.fiap.vaultix.domain.Cliente;

import java.time.LocalDate;

public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        LocalDate dtCadastro
) {
    public static ClienteResponse fromEntity(Cliente c) {
        return new ClienteResponse(
                c.getId(),
                c.getNome(),
                c.getEmail(),
                c.getCpf(),
                c.getTelefone(),
                c.getDtCadastro()
        );
    }
}
