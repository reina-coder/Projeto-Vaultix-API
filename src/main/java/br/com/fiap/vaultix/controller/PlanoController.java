package br.com.fiap.vaultix.controller;

import br.com.fiap.vaultix.dto.PlanoRequest;
import br.com.fiap.vaultix.dto.PlanoResponse;
import br.com.fiap.vaultix.service.PlanoService;
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

@RestController
@RequestMapping("/planos")
@Tag(name = "Planos de Assinatura", description = "Catálogo de planos comerciais (BASICO, PREMIUM, ENTERPRISE)")
public class PlanoController {

    private final PlanoService service;

    public PlanoController(PlanoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista planos paginados")
    public ResponseEntity<Page<PlanoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca plano por ID")
    public ResponseEntity<PlanoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cria novo plano")
    public ResponseEntity<PlanoResponse> criar(@Valid @RequestBody PlanoRequest req) {
        PlanoResponse criado = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza plano existente")
    public ResponseEntity<PlanoResponse> atualizar(@PathVariable Long id,
                                                   @Valid @RequestBody PlanoRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove plano (requer ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
