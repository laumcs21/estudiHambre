package com.estudiHambre.dto.notificacion;

import java.time.LocalDateTime;

public record NotificacionRespuestaDTO(
        Long id,
        String mensaje,
        boolean leida,
        LocalDateTime fechaCreacion
) {
}
