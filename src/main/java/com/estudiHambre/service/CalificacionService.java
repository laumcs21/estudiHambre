
package com.estudiHambre.service;

import com.estudiHambre.dto.calificacion.CalificacionActualizarDTO;
import com.estudiHambre.dto.calificacion.CalificacionCrearDTO;
import com.estudiHambre.dto.calificacion.CalificacionRespuestaDTO;
import com.estudiHambre.dto.calificacion.CalificacionResumenDTO;

import java.util.List;

public interface CalificacionService {

    CalificacionRespuestaDTO crear(
            Long establecimientoId,
            CalificacionCrearDTO dto,
            String correoUsuario
    );

    List<CalificacionRespuestaDTO> listarPorEstablecimiento(
            Long establecimientoId
    );

    CalificacionResumenDTO obtenerResumen(Long establecimientoId);

    CalificacionRespuestaDTO actualizar(
            Long calificacionId,
            CalificacionActualizarDTO dto,
            String correoUsuario
    );

    void eliminar(Long calificacionId, String correoUsuario);
}
