package com.fiap.floodmonitor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leituras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /** Nível da água em centímetros */
    @Column(nullable = false)
    private Double nivelAgua;

    /** Precipitação em mm/h */
    @Column(nullable = false)
    private Double precipitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivelAlerta;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum NivelAlerta {
        NORMAL,    // < 50 cm
        ATENCAO,   // 50–100 cm
        ALERTA,    // 100–150 cm
        CRITICO    // > 150 cm
    }

    /**
     * Calcula o nível de alerta automaticamente com base no nível da água.
     */
    public static NivelAlerta calcularNivel(Double nivelAgua) {
        if (nivelAgua < 50)  return NivelAlerta.NORMAL;
        if (nivelAgua < 100) return NivelAlerta.ATENCAO;
        if (nivelAgua < 150) return NivelAlerta.ALERTA;
        return NivelAlerta.CRITICO;
    }
}
