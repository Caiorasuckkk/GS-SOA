package com.fiap.floodmonitor.dto;

import com.fiap.floodmonitor.model.Alerta;
import com.fiap.floodmonitor.model.Leitura;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class AlertaDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotNull(message = "O ID do sensor é obrigatório.")
        private Long sensorId;

        private Long leituraId;

        @NotBlank(message = "A mensagem do alerta é obrigatória.")
        @Size(max = 500)
        private String mensagem;

        @NotNull(message = "O nível do alerta é obrigatório.")
        private Leitura.NivelAlerta nivel;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long sensorId;
        private String sensorNome;
        private Long leituraId;
        private String mensagem;
        private Leitura.NivelAlerta nivel;
        private LocalDateTime timestamp;
        private Boolean ativo;

        public static Response from(Alerta a) {
            return Response.builder()
                    .id(a.getId())
                    .sensorId(a.getSensor().getId())
                    .sensorNome(a.getSensor().getNome())
                    .leituraId(a.getLeitura() != null ? a.getLeitura().getId() : null)
                    .mensagem(a.getMensagem())
                    .nivel(a.getNivel())
                    .timestamp(a.getTimestamp())
                    .ativo(a.getAtivo())
                    .build();
        }
    }
}
