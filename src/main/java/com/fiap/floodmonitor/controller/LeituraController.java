package com.fiap.floodmonitor.controller;

import com.fiap.floodmonitor.dto.LeituraDTO;
import com.fiap.floodmonitor.exception.ApiResponse;
import com.fiap.floodmonitor.service.LeituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leituras")
@RequiredArgsConstructor
@Tag(name = "Leituras", description = "Registro e consulta de leituras dos sensores (nível de água e precipitação)")
public class LeituraController {

    private final LeituraService leituraService;

    @GetMapping
    @Operation(summary = "Lista todas as leituras")
    public ResponseEntity<ApiResponse<List<LeituraDTO.Response>>> listar(
            @Parameter(description = "Filtra por ID do sensor")
            @RequestParam(required = false) Long sensorId) {

        List<LeituraDTO.Response> dados = (sensorId != null)
                ? leituraService.listarPorSensor(sensorId)
                : leituraService.listarTodas();

        return ResponseEntity.ok(ApiResponse.ok(dados));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca leitura por ID")
    public ResponseEntity<ApiResponse<LeituraDTO.Response>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(leituraService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(
        summary = "Registra uma nova leitura",
        description = "Registra nível de água e precipitação. Gera alerta automático se nível for ALERTA ou CRITICO."
    )
    public ResponseEntity<ApiResponse<LeituraDTO.Response>> registrar(
            @Valid @RequestBody LeituraDTO.Request dto) {
        LeituraDTO.Response criada = leituraService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Leitura registrada.", criada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma leitura existente")
    public ResponseEntity<ApiResponse<LeituraDTO.Response>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LeituraDTO.Request dto) {
        return ResponseEntity.ok(ApiResponse.ok("Leitura atualizada.", leituraService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma leitura")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        leituraService.deletar(id);
        return ResponseEntity.ok(ApiResponse.ok("Leitura removida.", null));
    }
}
