
package com.estudiHambre.service;

import com.estudiHambre.dto.notificacion.NotificacionRespuestaDTO;
import com.estudiHambre.exception.RecursoNoEncontradoException;
import com.estudiHambre.model.Notificacion;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.NotificacionRepository;
import com.estudiHambre.repository.UsuarioRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository) {

        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacionRespuestaDTO> listarMisNotificaciones() {

        Usuario usuario = obtenerUsuarioAutenticado();

        return notificacionRepository
                .findByDestinatarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {

        Usuario usuario = obtenerUsuarioAutenticado();

        return notificacionRepository
                .countByDestinatarioIdAndLeidaFalse(usuario.getId());
    }

    @Transactional
    public NotificacionRespuestaDTO marcarComoLeida(Long id) {

        Usuario usuario = obtenerUsuarioAutenticado();

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Notificación no encontrada"
                ));

        if (!notificacion.getDestinatario().getId()
                .equals(usuario.getId())) {

            throw new AccessDeniedException(
                    "No tienes permiso para modificar esta notificación"
            );
        }

        notificacion.setLeida(true);

        return toDTO(notificacionRepository.save(notificacion));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearNotificacion(Usuario destinatario, String mensaje) {

        Usuario usuario = usuarioRepository.findById(destinatario.getId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Destinatario no encontrado"
                        ));

        Notificacion notificacion =
                new Notificacion(usuario, mensaje);

        notificacionRepository.save(notificacion);
    }


    private Usuario obtenerUsuarioAutenticado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión para consultar tus notificaciones"
            );
        }

        return usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado"
                ));
    }

    private NotificacionRespuestaDTO toDTO(Notificacion notificacion) {

        return new NotificacionRespuestaDTO(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.isLeida(),
                notificacion.getFechaCreacion()
        );
    }
}
