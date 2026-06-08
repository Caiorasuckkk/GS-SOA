package com.fiap.floodmonitor.controller;

import com.fiap.floodmonitor.dto.AlertaDTO;
import com.fiap.floodmonitor.exception.ApiResponse;
import com.fiap.floodmonitor.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gerenciamento de alertas de enchente — automáticos e manuais")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Lista todos os alertas")
    public ResponseEntity<ApiResponse<List<AlertaDTO.Response>>> listar(
            @Parameter(description = "Se true, retorna apenas alertas ativos")
            @RequestParam(required = false, defaultValue = "false") boolean apenasAtivos,
            @Parameter(description = "Filtra por ID do sensor")
            @RequestParam(required = false) Long sensorId) {

        List<AlertaDTO.Response> dados;
        if (sensorId != null) {
            dados = alertaService.listarPorSensor(sensorId);
        } else if (apenasAtivos) {
            dados = alertaService.listarAtivos();
        } else {
            dados = alertaService.listarTodos();
        }

        return ResponseEntity.ok(ApiResponse.ok(dados));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca alerta por ID")
    public ResponseEntity<ApiResponse<AlertaDTO.Response>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Cria um alerta manual")
    public ResponseEntity<ApiResponse<AlertaDTO.Response>> criar(
            @Valid @RequestBody AlertaDTO.Request dto) {
        AlertaDTO.Response criado = alertaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Alerta criado.", criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um alerta")
    public ResponseEntity<ApiResponse<AlertaDTO.Response>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlertaDTO.Request dto) {
        return ResponseEntity.ok(ApiResponse.ok("Alerta atualizado.", alertaService.atualizar(id, dto)));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativa (encerra) um alerta")
    public ResponseEntity<ApiResponse<AlertaDTO.Response>> desativar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Alerta desativado.", alertaService.desativar(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um alerta")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        alertaService.deletar(id);
        return ResponseEntity.ok(ApiResponse.ok("Alerta removido.", null));
    }
}
