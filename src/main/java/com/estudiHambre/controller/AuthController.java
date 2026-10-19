package com.estudiHambre.controller;

import com.estudiHambre.dto.usuario.auth.LoginDTO;
import com.estudiHambre.dto.usuario.auth.LoginRespuestaDTO;
import com.estudiHambre.dto.usuario.UsuarioRegistroDTO;
import com.estudiHambre.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @RequestBody UsuarioRegistroDTO dto) {

        try {
            LoginRespuestaDTO respuesta = authService.registrar(dto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(respuesta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @RequestBody LoginDTO dto) {

        try {
            LoginRespuestaDTO respuesta = authService.iniciarSesion(dto);

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }
    }
}