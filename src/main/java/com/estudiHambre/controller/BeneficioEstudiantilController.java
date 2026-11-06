package com.estudiHambre.controller;

import com.estudiHambre.dto.beneficio.BeneficioEstudiantilActualizarDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilCrearDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilRespuestaDTO;
import com.estudiHambre.service.BeneficioEstudiantilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BeneficioEstudiantilController {

    private final BeneficioEstudiantilService beneficioService;

    public BeneficioEstudiantilController(
            BeneficioEstudiantilService beneficioService
    ) {
        this.beneficioService = beneficioService;
    }

    @GetMapping("/api/establecimientos/{id}/beneficios-estudiantiles")
    public ResponseEntity<List<BeneficioEstudiantilRespuestaDTO>> listarPublicos(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(beneficioService.listarPublicos(id));
    }

    @GetMapping("/api/beneficios-estudiantiles/establecimiento/{id}")
    public ResponseEntity<List<BeneficioEstudiantilRespuestaDTO>> listarParaGestion(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(beneficioService.listarParaGestion(id));
    }

    @PostMapping("/api/establecimientos/{id}/beneficios-estudiantiles")
    public ResponseEntity<BeneficioEstudiantilRespuestaDTO> crear(
            @PathVariable Long id,
            @Valid @RequestBody BeneficioEstudiantilCrearDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beneficioService.crear(id, dto));
    }

    @PutMapping("/api/beneficios-estudiantiles/{id}")
    public ResponseEntity<BeneficioEstudiantilRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BeneficioEstudiantilActualizarDTO dto
    ) {
        return ResponseEntity.ok(beneficioService.actualizar(id, dto));
    }

    @DeleteMapping("/api/beneficios-estudiantiles/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        beneficioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
