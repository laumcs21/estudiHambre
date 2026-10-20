package com.estudiHambre.config;

import com.estudiHambre.exception.ErrorRespuesta;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityErrorHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void responder(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus estado,
            String mensaje) throws IOException {

        response.setStatus(estado.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(error)
        );
    }
}