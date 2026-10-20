package com.estudiHambre.service;

import com.estudiHambre.dto.usuario.auth.LoginDTO;
import com.estudiHambre.dto.usuario.auth.LoginRespuestaDTO;
import com.estudiHambre.dto.usuario.UsuarioRegistroDTO;
import com.estudiHambre.exception.ReglaNegocioException;
import com.estudiHambre.model.Rol;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginRespuestaDTO registrar(UsuarioRegistroDTO dto) {

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        validarContrasena(dto.getContrasena(), dto.getCorreo());

        String contrasenaCifrada =
                passwordEncoder.encode(dto.getContrasena());

        usuario.setContrasena(contrasenaCifrada);
        usuario.setRol(Rol.ESTUDIANTE);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return new LoginRespuestaDTO(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getCorreo(),
                usuarioGuardado.getRol(),
                "Usuario registrado correctamente"
        );
    }

    public LoginRespuestaDTO iniciarSesion(LoginDTO dto) {

        Usuario usuario = usuarioRepository
                .findByCorreo(dto.getCorreo())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Correo o contraseña incorrectos"
                        )
                );

        boolean contrasenaCorrecta = passwordEncoder.matches(
                dto.getContrasena(),
                usuario.getContrasena()
        );

        if (!contrasenaCorrecta) {
            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos"
            );
        }

        return new LoginRespuestaDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                "Inicio de sesión exitoso"
        );
    }

    private void validarContrasena(String contrasena, String correo) {

        if (contrasena == null || contrasena.isBlank()
                || contrasena.length() < 8) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos 8 caracteres"
            );
        }

        if (!contrasena.matches(".*[A-Z].*")) {
            throw new ReglaNegocioException(
                    "La contraseña debe contener al menos una letra mayúscula"
            );
        }

        if (!contrasena.matches(".*[a-z].*")) {
            throw new ReglaNegocioException(
                    "La contraseña debe contener al menos una letra minúscula"
            );
        }

        if (!contrasena.matches(".*[0-9].*")) {
            throw new ReglaNegocioException(
                    "La contraseña debe contener al menos un número"
            );
        }

        if (!contrasena.matches(".*[^a-zA-Z0-9\\s].*")) {
            throw new ReglaNegocioException(
                    "La contraseña debe contener al menos un carácter especial"
            );
        }

        String normalizada = contrasena.toLowerCase(Locale.ROOT);

        Set<String> comunes = Set.of(
                "123456789012",
                "password1234",
                "qwerty123456",
                "admin12345678",
                "contraseña123"
        );

        if (comunes.contains(normalizada)) {
            throw new ReglaNegocioException(
                    "La contraseña es demasiado común"
            );
        }

        if (correo != null && correo.contains("@")) {
            String nombreCorreo = correo.substring(0, correo.indexOf("@"))
                    .toLowerCase(Locale.ROOT);

            if (nombreCorreo.length() >= 3
                    && normalizada.contains(nombreCorreo)) {
                throw new ReglaNegocioException(
                        "La contraseña no puede contener el nombre de tu correo"
                );
            }
        }
    }
}