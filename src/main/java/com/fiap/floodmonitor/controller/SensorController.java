package com.fiap.floodmonitor.controller;

import com.fiap.floodmonitor.dto.SensorDTO;
import com.fiap.floodmonitor.exception.ApiResponse;
import com.fiap.floodmonitor.model.Sensor;
import com.fiap.floodmonitor.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensores")
@RequiredArgsConstructor
@Tag(name = "Sensores", description = "Gerenciamento dos sensores de monitoramento de enchentes")
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    @Operation(summary = "Lista todos os sensores", description = "Retorna a lista completa de sensores cadastrados.")
    public ResponseEntity<ApiResponse<List<SensorDTO.Response>>> listar(
            @Parameter(description = "Filtra por status: ATIVO, INATIVO, MANUTENCAO")
            @RequestParam(required = false) Sensor.StatusSensor status) {

        List<SensorDTO.Response> dados = (status != null)
                ? sensorService.listarPorStatus(status)
                : sensorService.listarTodos();

        return ResponseEntity.ok(ApiResponse.ok(dados));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca sensor por ID")
    public ResponseEntity<ApiResponse<SensorDTO.Response>> buscar(
            @Parameter(description = "ID do sensor") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sensorService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo sensor")
    public ResponseEntity<ApiResponse<SensorDTO.Response>> criar(
            @Valid @RequestBody SensorDTO.Request dto) {
        SensorDTO.Response criado = sensorService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Sensor cadastrado com sucesso.", criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados de um sensor")
    public ResponseEntity<ApiResponse<SensorDTO.Response>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SensorDTO.Request dto) {
        return ResponseEntity.ok(ApiResponse.ok("Sensor atualizado.", sensorService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um sensor")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        sensorService.deletar(id);
        return ResponseEntity.ok(ApiResponse.ok("Sensor removido com sucesso.", null));
    }
}
