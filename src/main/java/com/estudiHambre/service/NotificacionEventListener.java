
package com.estudiHambre.service;

import com.estudiHambre.event.EstablecimientoEvent;
import com.estudiHambre.model.Rol;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacionEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificacionEventListener.class);

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    public NotificacionEventListener(
            NotificacionService notificacionService,
            UsuarioRepository usuarioRepository) {

        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void procesarEvento(EstablecimientoEvent evento) {

        try {
            switch (evento.tipo()) {

                case CREADO -> notificarAdministradores(evento);

                case APROBADO -> notificarPropietario(
                        evento,
                        "Tu establecimiento '" +
                                evento.nombreEstablecimiento() +
                                "' fue aprobado"
                );

                case RECHAZADO -> notificarPropietario(
                        evento,
                        "Tu establecimiento '" +
                                evento.nombreEstablecimiento() +
                                "' fue rechazado"
                );
            }

        } catch (Exception e) {
            log.error(
                    "Error al generar notificaciones del establecimiento {}",
                    evento.establecimientoId(),
                    e
            );
        }
    }

    private void notificarAdministradores(EstablecimientoEvent evento) {

        for (Usuario administrador :
                usuarioRepository.findByRol(Rol.ADMIN)) {

            guardarNotificacion(
                    administrador,
                    "El establecimiento '" +
                            evento.nombreEstablecimiento() +
                            "' está pendiente de aprobación"
            );
        }
    }

    private void notificarPropietario(
            EstablecimientoEvent evento,
            String mensaje) {

        Usuario propietario = usuarioRepository
                .findById(evento.propietarioId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Propietario no encontrado"
                        ));

        guardarNotificacion(propietario, mensaje);
    }

    private void guardarNotificacion(
            Usuario destinatario,
            String mensaje) {

        notificacionService.crearNotificacion(
                destinatario,
                mensaje
        );
    }
}
