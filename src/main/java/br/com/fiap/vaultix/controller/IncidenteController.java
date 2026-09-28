package br.com.fiap.vaultix.controller;

import br.com.fiap.vaultix.dto.IncidenteRequest;
import br.com.fiap.vaultix.dto.IncidenteResponse;
import br.com.fiap.vaultix.dto.IncidenteStatusRequest;
import br.com.fiap.vaultix.service.IncidenteService;
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
@RequestMapping("/incidentes")
@Tag(name = "Incidentes", description = "Registro e gestão de incidentes de identidade digital")
public class IncidenteController {

    private final IncidenteService service;

    public IncidenteController(IncidenteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista incidentes paginados")
    public ResponseEntity<Page<IncidenteResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca incidente por ID")
    public ResponseEntity<IncidenteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @Operation(summary = "Histórico de incidentes de um cliente")
    public ResponseEntity<List<IncidenteResponse>> historico(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.historicoPorCliente(idCliente));
    }

    @PostMapping
    @Operation(summary = "Registra novo incidente. Incidentes CRITICA viram EM_ATENDIMENTO via trigger.")
    public ResponseEntity<IncidenteResponse> criar(@Valid @RequestBody IncidenteRequest req) {
        IncidenteResponse criado = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza incidente existente")
    public ResponseEntity<IncidenteResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody IncidenteRequest req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza apenas o status. Mudar para RESOLVIDO dispara trigger de auditoria.")
    public ResponseEntity<IncidenteResponse> atualizarStatus(@PathVariable Long id,
                                                             @Valid @RequestBody IncidenteStatusRequest req) {
        return ResponseEntity.ok(service.atualizarStatus(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove incidente (requer ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
