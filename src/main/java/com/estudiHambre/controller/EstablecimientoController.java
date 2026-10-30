package com.estudiHambre.controller;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.service.EstablecimientoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/establecimientos")
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;

    public EstablecimientoController(
            EstablecimientoService establecimientoService) {
        this.establecimientoService = establecimientoService;
    }

    // Consultar establecimientos públicos


    @GetMapping
    public ResponseEntity<List<EstablecimientoRespuestaDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String plato,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) Double radioKm) {

        return ResponseEntity.ok(
                establecimientoService.filtrarEstablecimientos(
                        nombre,
                        tipo,
                        plato,
                        precioMax,
                        latitud,
                        longitud,
                        radioKm
                )
        );
    }


    // Consultar mis establecimientos
    @GetMapping("/mis-establecimientos")
    public ResponseEntity<List<EstablecimientoRespuestaDTO>>
    misEstablecimientos() {

        return ResponseEntity.ok(
                establecimientoService.listarMisEstablecimientos()
        );
    }

    // Consultar un establecimiento por ID
    @GetMapping("/{id}")
    public ResponseEntity<EstablecimientoRespuestaDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                establecimientoService.buscarPorId(id)
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EstablecimientoRespuestaDTO> crear(
            @Valid @RequestPart("datos") EstablecimientoCrearDTO dto,
            @RequestPart("imagen") MultipartFile imagen) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(establecimientoService.crear(dto, imagen));
    }

    // Actualizar un establecimiento
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EstablecimientoRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestPart("datos") EstablecimientoActualizarDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        return ResponseEntity.ok(
                establecimientoService.actualizar(id, dto, imagen)
        );
    }

    // Eliminar un establecimiento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        establecimientoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long id) {

        establecimientoService.eliminarImagen(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/cercanos")
    public ResponseEntity<List<EstablecimientoRespuestaDTO>> buscarCercanos(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam Double radioKm) {

        return ResponseEntity.ok(
                establecimientoService.buscarCercanos(
                        latitud,
                        longitud,
                        radioKm
                )
        );
    }

}