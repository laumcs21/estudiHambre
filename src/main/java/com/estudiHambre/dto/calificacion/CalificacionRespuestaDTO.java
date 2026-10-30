
package com.estudiHambre.dto.calificacion;

public record CalificacionRespuestaDTO(
        Long id,
        Integer puntuacion,
        Long usuarioId,
        String nombreUsuario,
        Long establecimientoId
) {
}
