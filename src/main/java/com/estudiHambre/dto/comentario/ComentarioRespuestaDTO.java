
package com.estudiHambre.dto.comentario;

import com.estudiHambre.model.EstadoComentario;
import java.time.LocalDateTime;

public record ComentarioRespuestaDTO(

        Long id,
        String contenido,
        LocalDateTime fecha,
        EstadoComentario estado,
        Long usuarioId,
        String nombreUsuario,
        Long establecimientoId

) {
}
