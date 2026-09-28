package br.com.fiap.vaultix.controller;

import br.com.fiap.vaultix.dto.AuditoriaResponse;
import br.com.fiap.vaultix.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
@Tag(name = "Auditoria", description = "Trilha de auditoria de compliance (LGPD). Somente leitura - registros são gerados via trigger PL/SQL.")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os registros de auditoria paginados")
    public ResponseEntity<Page<AuditoriaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca registro de auditoria por ID")
    public ResponseEntity<AuditoriaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @Operation(summary = "Trilha de auditoria de um cliente específico")
    public ResponseEntity<List<AuditoriaResponse>> porCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @GetMapping("/incidente/{idIncidente}")
    @Operation(summary = "Registros de auditoria de um incidente específico")
    public ResponseEntity<List<AuditoriaResponse>> porIncidente(@PathVariable Long idIncidente) {
        return ResponseEntity.ok(service.listarPorIncidente(idIncidente));
    }
}
