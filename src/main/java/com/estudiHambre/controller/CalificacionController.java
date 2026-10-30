
package com.estudiHambre.controller;

import com.estudiHambre.dto.calificacion.CalificacionActualizarDTO;
import com.estudiHambre.dto.calificacion.CalificacionCrearDTO;
import com.estudiHambre.dto.calificacion.CalificacionRespuestaDTO;
import com.estudiHambre.dto.calificacion.CalificacionResumenDTO;
import com.estudiHambre.service.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PostMapping("/api/establecimientos/{id}/calificaciones")
    public ResponseEntity<CalificacionRespuestaDTO> crear(
            @PathVariable Long id,
            @Valid @RequestBody CalificacionCrearDTO dto,
            Authentication authentication
    ) {
        CalificacionRespuestaDTO respuesta = calificacionService.crear(
                id,
                dto,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/api/establecimientos/{id}/calificaciones")
    public ResponseEntity<List<CalificacionRespuestaDTO>> listar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                calificacionService.listarPorEstablecimiento(id)
        );
    }

    @GetMapping("/api/establecimientos/{id}/calificaciones/resumen")
    public ResponseEntity<CalificacionResumenDTO> resumen(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                calificacionService.obtenerResumen(id)
        );
    }

    @PutMapping("/api/calificaciones/{id}")
    public ResponseEntity<CalificacionRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CalificacionActualizarDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                calificacionService.actualizar(
                        id,
                        dto,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/api/calificaciones/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        calificacionService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
