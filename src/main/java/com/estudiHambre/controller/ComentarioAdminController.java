
package com.estudiHambre.controller;

import com.estudiHambre.dto.comentario.ComentarioRespuestaDTO;
import com.estudiHambre.service.ComentarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comentarios")
public class ComentarioAdminController {

    private final ComentarioService comentarioService;

    public ComentarioAdminController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/establecimientos/{establecimientoId}")
    public ResponseEntity<List<ComentarioRespuestaDTO>> listarTodos(
            @PathVariable Long establecimientoId
    ) {
        return ResponseEntity.ok(
                comentarioService.listarTodosParaAdministracion(
                        establecimientoId
                )
        );
    }

    @PatchMapping("/{id}/ocultar")
    public ResponseEntity<ComentarioRespuestaDTO> ocultar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(comentarioService.ocultar(id));
    }

    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<ComentarioRespuestaDTO> restaurar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(comentarioService.restaurar(id));
    }
}
