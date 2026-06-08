package com.fiap.floodmonitor.repository;

import com.fiap.floodmonitor.model.Leitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeituraRepository extends JpaRepository<Leitura, Long> {

    List<Leitura> findBySensorIdOrderByTimestampDesc(Long sensorId);

    Optional<Leitura> findTopBySensorIdOrderByTimestampDesc(Long sensorId);

    List<Leitura> findByNivelAlerta(Leitura.NivelAlerta nivelAlerta);
}
