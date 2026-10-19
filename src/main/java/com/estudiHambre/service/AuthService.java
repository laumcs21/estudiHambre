package com.estudiHambre.service;

import com.estudiHambre.dto.usuario.auth.LoginDTO;
import com.estudiHambre.dto.usuario.auth.LoginRespuestaDTO;
import com.estudiHambre.dto.usuario.UsuarioRegistroDTO;
import com.estudiHambre.model.Rol;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
}