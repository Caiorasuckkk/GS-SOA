package com.fiap.floodmonitor.repository;

import com.fiap.floodmonitor.model.Alerta;
import com.fiap.floodmonitor.model.Leitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findBySensorIdOrderByTimestampDesc(Long sensorId);

    List<Alerta> findByAtivoTrue();

    List<Alerta> findByNivelOrderByTimestampDesc(Leitura.NivelAlerta nivel);
}
