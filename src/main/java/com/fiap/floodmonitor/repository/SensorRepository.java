package com.fiap.floodmonitor.repository;

import com.fiap.floodmonitor.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findByStatus(Sensor.StatusSensor status);

    boolean existsByNomeAndLocalizacao(String nome, String localizacao);
}
