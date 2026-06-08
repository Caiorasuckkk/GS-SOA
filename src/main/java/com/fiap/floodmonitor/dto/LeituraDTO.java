package com.fiap.floodmonitor.dto;

import com.fiap.floodmonitor.model.Leitura;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class LeituraDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotNull(message = "O ID do sensor é obrigatório.")
        private Long sensorId;

        @NotNull(message = "O nível da água é obrigatório.")
        @DecimalMin(value = "0.0", message = "Nível da água não pode ser negativo.")
        private Double nivelAgua;

        @NotNull(message = "A precipitação é obrigatória.")
        @DecimalMin(value = "0.0", message = "Precipitação não pode ser negativa.")
        private Double precipitacao;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long sensorId;
        private String sensorNome;
        private Double nivelAgua;
        private Double precipitacao;
        private Leitura.NivelAlerta nivelAlerta;
        private LocalDateTime timestamp;

        public static Response from(Leitura l) {
            return Response.builder()
                    .id(l.getId())
                    .sensorId(l.getSensor().getId())
                    .sensorNome(l.getSensor().getNome())
                    .nivelAgua(l.getNivelAgua())
                    .precipitacao(l.getPrecipitacao())
                    .nivelAlerta(l.getNivelAlerta())
                    .timestamp(l.getTimestamp())
                    .build();
        }
    }
}
