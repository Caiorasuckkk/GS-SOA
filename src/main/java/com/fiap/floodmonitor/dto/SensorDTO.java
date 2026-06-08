package com.fiap.floodmonitor.dto;

import com.fiap.floodmonitor.model.Sensor;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class SensorDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotBlank(message = "O nome do sensor é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        private String nome;

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres.")
        private String descricao;

        @NotBlank(message = "A localização é obrigatória.")
        private String localizacao;

        @NotNull(message = "A latitude é obrigatória.")
        @DecimalMin(value = "-90.0", message = "Latitude inválida.")
        @DecimalMax(value = "90.0",  message = "Latitude inválida.")
        private Double latitude;

        @NotNull(message = "A longitude é obrigatória.")
        @DecimalMin(value = "-180.0", message = "Longitude inválida.")
        @DecimalMax(value = "180.0",  message = "Longitude inválida.")
        private Double longitude;

        private Sensor.StatusSensor status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String nome;
        private String descricao;
        private String localizacao;
        private Double latitude;
        private Double longitude;
        private Sensor.StatusSensor status;
        private LocalDateTime criadoEm;

        public static Response from(Sensor s) {
            return Response.builder()
                    .id(s.getId())
                    .nome(s.getNome())
                    .descricao(s.getDescricao())
                    .localizacao(s.getLocalizacao())
                    .latitude(s.getLatitude())
                    .longitude(s.getLongitude())
                    .status(s.getStatus())
                    .criadoEm(s.getCriadoEm())
                    .build();
        }
    }
}
