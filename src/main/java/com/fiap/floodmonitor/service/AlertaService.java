package com.fiap.floodmonitor.service;

import com.fiap.floodmonitor.dto.AlertaDTO;
import com.fiap.floodmonitor.exception.ResourceNotFoundException;
import com.fiap.floodmonitor.model.*;
import com.fiap.floodmonitor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final SensorService sensorService;
    private final LeituraRepository leituraRepository;

    public List<AlertaDTO.Response> listarTodos() {
        return alertaRepository.findAll()
                .stream()
                .map(AlertaDTO.Response::from)
                .collect(Collectors.toList());
    }

    public List<AlertaDTO.Response> listarAtivos() {
        return alertaRepository.findByAtivoTrue()
                .stream()
                .map(AlertaDTO.Response::from)
                .collect(Collectors.toList());
    }

    public AlertaDTO.Response buscarPorId(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
        return AlertaDTO.Response.from(alerta);
    }

    public List<AlertaDTO.Response> listarPorSensor(Long sensorId) {
        sensorService.buscarEntidade(sensorId);
        return alertaRepository.findBySensorIdOrderByTimestampDesc(sensorId)
                .stream()
                .map(AlertaDTO.Response::from)
                .collect(Collectors.toList());
    }

    public AlertaDTO.Response criar(AlertaDTO.Request dto) {
        Sensor sensor = sensorService.buscarEntidade(dto.getSensorId());

        Leitura leitura = null;
        if (dto.getLeituraId() != null) {
            leitura = leituraRepository.findById(dto.getLeituraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Leitura", dto.getLeituraId()));
        }

        Alerta alerta = Alerta.builder()
                .sensor(sensor)
                .leitura(leitura)
                .mensagem(dto.getMensagem())
                .nivel(dto.getNivel())
                .build();

        return AlertaDTO.Response.from(alertaRepository.save(alerta));
    }

    public AlertaDTO.Response atualizar(Long id, AlertaDTO.Request dto) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));

        Sensor sensor = sensorService.buscarEntidade(dto.getSensorId());
        alerta.setSensor(sensor);
        alerta.setMensagem(dto.getMensagem());
        alerta.setNivel(dto.getNivel());

        return AlertaDTO.Response.from(alertaRepository.save(alerta));
    }

    /** Desativa um alerta (soft delete / encerramento) */
    public AlertaDTO.Response desativar(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
        alerta.setAtivo(false);
        return AlertaDTO.Response.from(alertaRepository.save(alerta));
    }

    public void deletar(Long id) {
        if (!alertaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta", id);
        }
        alertaRepository.deleteById(id);
    }
}
