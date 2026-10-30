
package com.estudiHambre.service;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface EstablecimientoService {

    List<EstablecimientoRespuestaDTO> listarTodos();

    EstablecimientoRespuestaDTO buscarPorId(Long id);

    List<EstablecimientoRespuestaDTO> buscarPorNombre(String nombre);

    List<EstablecimientoRespuestaDTO> buscarPorTipo(String tipo);

    List<EstablecimientoRespuestaDTO> listarMisEstablecimientos();

    EstablecimientoRespuestaDTO crear(
            EstablecimientoCrearDTO dto,
            MultipartFile imagen
    );

    EstablecimientoRespuestaDTO actualizar(
            Long id,
            EstablecimientoActualizarDTO dto,
            MultipartFile imagen
    );

    void eliminar(Long id);

    List<EstablecimientoRespuestaDTO> listarPendientes();

    EstablecimientoRespuestaDTO aprobar(Long id);

    EstablecimientoRespuestaDTO rechazar(Long id);

    void eliminarImagen(Long id);

    List<EstablecimientoRespuestaDTO> listarTodosAdmin();

    List<EstablecimientoRespuestaDTO> buscarCercanos(
            Double latitud,
            Double longitud,
            Double radioKm
    );


    List<EstablecimientoRespuestaDTO> filtrarEstablecimientos(
            String nombre,
            String tipo,
            String plato,
            BigDecimal precioMax,
            Double latitud,
            Double longitud,
            Double radioKm
    );

}
