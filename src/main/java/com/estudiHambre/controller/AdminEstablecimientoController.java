package com.estudiHambre.controller;

import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.service.EstablecimientoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/establecimientos")
public class AdminEstablecimientoController {

    private final EstablecimientoService establecimientoService;

    public AdminEstablecimientoController(
            EstablecimientoService establecimientoService) {
        this.establecimientoService = establecimientoService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<EstablecimientoRespuestaDTO>> listarPendientes() {

        return ResponseEntity.ok(
                establecimientoService.listarPendientes()
        );
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<EstablecimientoRespuestaDTO> aprobar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                establecimientoService.aprobar(id)
        );
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<EstablecimientoRespuestaDTO> rechazar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                establecimientoService.rechazar(id)
        );
    }
}