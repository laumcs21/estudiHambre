package com.estudiHambre.exception;

import java.time.LocalDateTime;

public record ErrorRespuesta(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta
) {
}