
package com.estudiHambre.service;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import org.springframework.web.multipart.MultipartFile;

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
}
