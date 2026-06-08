package com.fiap.floodmonitor.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flood Monitor API")
                        .description("""
                                🌊 **Sistema de Monitoramento de Enchentes**
                                
                                API REST desenvolvida para a Global Solution 2026 — FIAP (ODS 9).
                                
                                Permite o cadastro de sensores, registro de leituras de nível de água
                                e gerenciamento de alertas, com geração automática de alertas críticos.
                                
                                **Autenticação:** Todas as rotas exigem o header `X-API-KEY`.
                                Utilize `FIAP-GS-2026-FLOOD-KEY` para testes locais.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe FIAP — 3ESPY 2026")
                                .email("alunos@fiap.com.br")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-KEY")));
    }
}
