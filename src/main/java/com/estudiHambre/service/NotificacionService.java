
package com.estudiHambre.service;

import com.estudiHambre.dto.notificacion.NotificacionRespuestaDTO;
import com.estudiHambre.model.Usuario;

import java.util.List;

public interface NotificacionService {

    List<NotificacionRespuestaDTO> listarMisNotificaciones();

    long contarNoLeidas();

    NotificacionRespuestaDTO marcarComoLeida(Long id);

    void crearNotificacion(Usuario destinatario, String mensaje);
}
