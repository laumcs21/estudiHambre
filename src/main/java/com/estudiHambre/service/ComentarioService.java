
package com.estudiHambre.service;

import com.estudiHambre.dto.comentario.ComentarioCrearDTO;
import com.estudiHambre.dto.comentario.ComentarioActualizarDTO;
import com.estudiHambre.dto.comentario.ComentarioRespuestaDTO;

import java.util.List;

public interface ComentarioService {

    ComentarioRespuestaDTO crear(
            Long establecimientoId,
            ComentarioCrearDTO dto,
            String correoUsuario
    );

    List<ComentarioRespuestaDTO> listarVisibles(
            Long establecimientoId
    );

    ComentarioRespuestaDTO actualizar(
            Long comentarioId,
            ComentarioActualizarDTO dto,
            String correoUsuario
    );

    void eliminar(Long comentarioId, String correoUsuario);

    List<ComentarioRespuestaDTO> listarTodosParaAdministracion(
            Long establecimientoId
    );

    ComentarioRespuestaDTO ocultar(Long comentarioId);

    ComentarioRespuestaDTO restaurar(Long comentarioId);
}
