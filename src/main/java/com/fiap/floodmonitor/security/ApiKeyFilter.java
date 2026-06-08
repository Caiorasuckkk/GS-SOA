package com.fiap.floodmonitor.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ApiKeyFilter implements Filter {

    @Value("${app.security.api-key}")
    private String validApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    // Endpoints públicos (sem necessidade de API Key)
    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui", "/v3/api-docs", "/api-docs",
            "/h2-console", "/actuator"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        // Libera endpoints públicos
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        String apiKey = httpReq.getHeader("X-API-KEY");

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            httpResp.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResp.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResp.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "success",   false,
                    "message",   "Acesso não autorizado. Forneça um X-API-KEY válido.",
                    "timestamp", LocalDateTime.now().toString()
            )));
            return;
        }

        chain.doFilter(request, response);
    }
}
