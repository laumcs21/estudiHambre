
package com.estudiHambre.service;

import com.estudiHambre.dto.menu.MenuActualizarDTO;
import com.estudiHambre.dto.menu.MenuCrearDTO;
import com.estudiHambre.dto.menu.MenuRespuestaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MenuService {

    List<MenuRespuestaDTO> listarTodos();

    MenuRespuestaDTO buscarPorId(Long id);

    List<MenuRespuestaDTO> buscarPorEstablecimiento(
            Long establecimientoId
    );

    MenuRespuestaDTO crear(
            MenuCrearDTO dto,
            List<MultipartFile> imagenes
    );

    MenuRespuestaDTO actualizar(
            Long id,
            MenuActualizarDTO dto,
            List<MultipartFile> imagenes
    );

    void eliminar(Long id);

    void eliminarImagen(Long menuId, int indice);
}
