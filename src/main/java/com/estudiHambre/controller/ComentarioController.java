
package com.estudiHambre.controller;

import com.estudiHambre.dto.comentario.ComentarioActualizarDTO;
import com.estudiHambre.dto.comentario.ComentarioCrearDTO;
import com.estudiHambre.dto.comentario.ComentarioRespuestaDTO;
import com.estudiHambre.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping("/api/establecimientos/{id}/comentarios")
    public ResponseEntity<ComentarioRespuestaDTO> crear(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioCrearDTO dto,
            Authentication authentication
    ) {
        ComentarioRespuestaDTO respuesta = comentarioService.crear(
                id,
                dto,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @GetMapping("/api/establecimientos/{id}/comentarios")
    public ResponseEntity<List<ComentarioRespuestaDTO>> listar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                comentarioService.listarVisibles(id)
        );
    }

    @PutMapping("/api/comentarios/{id}")
    public ResponseEntity<ComentarioRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioActualizarDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                comentarioService.actualizar(
                        id,
                        dto,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/api/comentarios/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        comentarioService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
