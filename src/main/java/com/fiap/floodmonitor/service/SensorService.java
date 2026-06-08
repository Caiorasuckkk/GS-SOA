package com.fiap.floodmonitor.service;

import com.fiap.floodmonitor.dto.SensorDTO;
import com.fiap.floodmonitor.exception.ResourceNotFoundException;
import com.fiap.floodmonitor.model.Sensor;
import com.fiap.floodmonitor.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    public List<SensorDTO.Response> listarTodos() {
        return sensorRepository.findAll()
                .stream()
                .map(SensorDTO.Response::from)
                .collect(Collectors.toList());
    }

    public SensorDTO.Response buscarPorId(Long id) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
        return SensorDTO.Response.from(sensor);
    }

    public List<SensorDTO.Response> listarPorStatus(Sensor.StatusSensor status) {
        return sensorRepository.findByStatus(status)
                .stream()
                .map(SensorDTO.Response::from)
                .collect(Collectors.toList());
    }

    public SensorDTO.Response criar(SensorDTO.Request dto) {
        Sensor sensor = Sensor.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .localizacao(dto.getLocalizacao())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .status(dto.getStatus() != null ? dto.getStatus() : Sensor.StatusSensor.ATIVO)
                .build();
        return SensorDTO.Response.from(sensorRepository.save(sensor));
    }

    public SensorDTO.Response atualizar(Long id, SensorDTO.Request dto) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));

        sensor.setNome(dto.getNome());
        sensor.setDescricao(dto.getDescricao());
        sensor.setLocalizacao(dto.getLocalizacao());
        sensor.setLatitude(dto.getLatitude());
        sensor.setLongitude(dto.getLongitude());
        if (dto.getStatus() != null) sensor.setStatus(dto.getStatus());

        return SensorDTO.Response.from(sensorRepository.save(sensor));
    }

    public void deletar(Long id) {
        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor", id);
        }
        sensorRepository.deleteById(id);
    }

    /** Método utilitário usado pelos outros services */
    public Sensor buscarEntidade(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }
}
