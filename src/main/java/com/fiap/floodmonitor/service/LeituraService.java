package com.fiap.floodmonitor.service;

import com.fiap.floodmonitor.dto.LeituraDTO;
import com.fiap.floodmonitor.exception.ResourceNotFoundException;
import com.fiap.floodmonitor.model.*;
import com.fiap.floodmonitor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeituraService {

    private final LeituraRepository leituraRepository;
    private final SensorService sensorService;
    private final AlertaRepository alertaRepository;

    public List<LeituraDTO.Response> listarTodas() {
        return leituraRepository.findAll()
                .stream()
                .map(LeituraDTO.Response::from)
                .collect(Collectors.toList());
    }

    public LeituraDTO.Response buscarPorId(Long id) {
        Leitura leitura = leituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leitura", id));
        return LeituraDTO.Response.from(leitura);
    }

    public List<LeituraDTO.Response> listarPorSensor(Long sensorId) {
        // Valida que o sensor existe
        sensorService.buscarEntidade(sensorId);
        return leituraRepository.findBySensorIdOrderByTimestampDesc(sensorId)
                .stream()
                .map(LeituraDTO.Response::from)
                .collect(Collectors.toList());
    }

    public LeituraDTO.Response registrar(LeituraDTO.Request dto) {
        Sensor sensor = sensorService.buscarEntidade(dto.getSensorId());

        Leitura.NivelAlerta nivel = Leitura.calcularNivel(dto.getNivelAgua());

        Leitura leitura = Leitura.builder()
                .sensor(sensor)
                .nivelAgua(dto.getNivelAgua())
                .precipitacao(dto.getPrecipitacao())
                .nivelAlerta(nivel)
                .build();

        leitura = leituraRepository.save(leitura);

        // Gera alerta automático se nível for ALERTA ou CRITICO
        if (nivel == Leitura.NivelAlerta.ALERTA || nivel == Leitura.NivelAlerta.CRITICO) {
            Alerta alerta = Alerta.builder()
                    .sensor(sensor)
                    .leitura(leitura)
                    .nivel(nivel)
                    .mensagem(String.format(
                            "[AUTO] Sensor '%s' em %s: nível da água %.1f cm — status %s.",
                            sensor.getNome(), sensor.getLocalizacao(), dto.getNivelAgua(), nivel))
                    .build();
            alertaRepository.save(alerta);
        }

        return LeituraDTO.Response.from(leitura);
    }

    public LeituraDTO.Response atualizar(Long id, LeituraDTO.Request dto) {
        Leitura leitura = leituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leitura", id));

        Sensor sensor = sensorService.buscarEntidade(dto.getSensorId());
        leitura.setSensor(sensor);
        leitura.setNivelAgua(dto.getNivelAgua());
        leitura.setPrecipitacao(dto.getPrecipitacao());
        leitura.setNivelAlerta(Leitura.calcularNivel(dto.getNivelAgua()));

        return LeituraDTO.Response.from(leituraRepository.save(leitura));
    }

    public void deletar(Long id) {
        if (!leituraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leitura", id);
        }
        leituraRepository.deleteById(id);
    }
}
