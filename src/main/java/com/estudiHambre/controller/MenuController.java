
package com.estudiHambre.controller;

import com.estudiHambre.dto.menu.MenuActualizarDTO;
import com.estudiHambre.dto.menu.MenuCrearDTO;
import com.estudiHambre.dto.menu.MenuRespuestaDTO;
import com.estudiHambre.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuRespuestaDTO>> listarTodos() {
        return ResponseEntity.ok(menuService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuRespuestaDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(menuService.buscarPorId(id));
    }

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<List<MenuRespuestaDTO>> buscarPorEstablecimiento(
            @PathVariable Long establecimientoId) {

        return ResponseEntity.ok(
                menuService.buscarPorEstablecimiento(establecimientoId)
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuRespuestaDTO> crear(
            @Valid @RequestPart("datos") MenuCrearDTO dto,
            @RequestPart(value = "imagenes", required = false)
            List<MultipartFile> imagenes) {

        MenuRespuestaDTO respuesta = menuService.crear(dto, imagenes);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MenuRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestPart("datos") MenuActualizarDTO dto,
            @RequestPart(value = "imagenes", required = false)
            List<MultipartFile> imagenes) {

        return ResponseEntity.ok(
                menuService.actualizar(id, dto, imagenes)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        menuService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{menuId}/imagenes/{indice}")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long menuId,
            @PathVariable int indice) {

        menuService.eliminarImagen(menuId, indice);

        return ResponseEntity.noContent().build();
    }
}
