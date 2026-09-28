package br.com.fiap.vaultix.controller;

import br.com.fiap.vaultix.dto.AssinaturaRequest;
import br.com.fiap.vaultix.dto.AssinaturaResponse;
import br.com.fiap.vaultix.service.AssinaturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/assinaturas")
@Tag(name = "Assinaturas", description = "Vínculo cliente-plano com vigência e status")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista assinaturas paginadas")
    public ResponseEntity<Page<AssinaturaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca assinatura por ID")
    public ResponseEntity<AssinaturaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @Operation(summary = "Lista assinaturas de um cliente específico")
    public ResponseEntity<List<AssinaturaResponse>> porCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @PostMapping
    @Operation(summary = "Cria nova assinatura")
    public ResponseEntity<AssinaturaResponse> criar(@Valid @RequestBody AssinaturaRequest req) {
        AssinaturaResponse criada = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza assinatura existente. Mudar status para VENCIDA dispara trigger de auditoria.")
    public ResponseEntity<AssinaturaResponse> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody AssinaturaRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove assinatura (requer ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
