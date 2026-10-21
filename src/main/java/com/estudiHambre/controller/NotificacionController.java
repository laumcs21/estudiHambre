
package com.estudiHambre.controller;

import com.estudiHambre.dto.notificacion.NotificacionRespuestaDTO;
import com.estudiHambre.service.NotificacionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(
            NotificacionService notificacionService) {

        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionRespuestaDTO>> listar() {

        return ResponseEntity.ok(
                notificacionService.listarMisNotificaciones()
        );
    }

    @GetMapping("/no-leidas/contador")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {

        return ResponseEntity.ok(
                Map.of("cantidad", notificacionService.contarNoLeidas())
        );
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionRespuestaDTO> marcarComoLeida(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificacionService.marcarComoLeida(id)
        );
    }
}
