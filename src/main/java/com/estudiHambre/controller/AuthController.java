package com.estudiHambre.controller;

import com.estudiHambre.dto.usuario.UsuarioRegistroDTO;
import com.estudiHambre.dto.usuario.auth.LoginDTO;
import com.estudiHambre.dto.usuario.auth.LoginRespuestaDTO;
import com.estudiHambre.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private final SessionAuthenticationStrategy sessionStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager) {

        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @Valid @RequestBody UsuarioRegistroDTO dto) {

        try {
            LoginRespuestaDTO respuesta = authService.registrar(dto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(respuesta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    dto.getCorreo(),
                                    dto.getContrasena()
                            )
                    );

            sessionStrategy.onAuthentication(
                    authentication, request, response
            );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context, request, response
            );

            LoginRespuestaDTO resultado =
                    authService.iniciarSesion(dto);

            return ResponseEntity.ok(resultado);
    }

    @GetMapping("/csrf")
    public ResponseEntity<?> obtenerCsrf(CsrfToken token) {
        return ResponseEntity.ok(
                java.util.Map.of(
                        "headerName", token.getHeaderName(),
                        "token", token.getToken()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> usuarioActual(
            Authentication authentication) {

        return ResponseEntity.ok(
                java.util.Map.of(
                        "correo", authentication.getName(),
                        "autenticado", true
                )
        );
    }
}