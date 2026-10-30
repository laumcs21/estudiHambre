
package com.estudiHambre.service.impl;

import com.estudiHambre.dto.calificacion.CalificacionActualizarDTO;
import com.estudiHambre.dto.calificacion.CalificacionCrearDTO;
import com.estudiHambre.dto.calificacion.CalificacionRespuestaDTO;
import com.estudiHambre.dto.calificacion.CalificacionResumenDTO;
import com.estudiHambre.model.Calificacion;
import com.estudiHambre.model.Establecimiento;
import com.estudiHambre.model.EstadoEstablecimiento;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.CalificacionRepository;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.UsuarioRepository;
import com.estudiHambre.service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public CalificacionServiceImpl(
            CalificacionRepository calificacionRepository,
            EstablecimientoRepository establecimientoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.calificacionRepository = calificacionRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public CalificacionRespuestaDTO crear(
            Long establecimientoId,
            CalificacionCrearDTO dto,
            String correoUsuario
    ) {
        validarPuntuacion(dto.puntuacion());

        Usuario usuario = obtenerUsuario(correoUsuario);
        Establecimiento establecimiento =
                obtenerEstablecimientoAprobado(establecimientoId);

        boolean yaCalifico =
                calificacionRepository.existsByUsuarioIdAndEstablecimientoId(
                        usuario.getId(),
                        establecimientoId
                );

        if (yaCalifico) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya calificaste este establecimiento. Puedes actualizar tu calificación."
            );
        }

        Calificacion calificacion = new Calificacion(
                dto.puntuacion(),
                usuario,
                establecimiento
        );

        Calificacion guardada = calificacionRepository.save(calificacion);

        return convertirRespuesta(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalificacionRespuestaDTO> listarPorEstablecimiento(
            Long establecimientoId
    ) {
        obtenerEstablecimientoAprobado(establecimientoId);

        return calificacionRepository
                .findByEstablecimientoId(establecimientoId)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalificacionResumenDTO obtenerResumen(Long establecimientoId) {

        obtenerEstablecimientoAprobado(establecimientoId);

        Double promedio =
                calificacionRepository.calcularPromedio(establecimientoId);

        long total =
                calificacionRepository.countByEstablecimientoId(
                        establecimientoId
                );

        return new CalificacionResumenDTO(
                establecimientoId,
                promedio == null ? 0.0 : promedio,
                total
        );
    }

    @Override
    @Transactional
    public CalificacionRespuestaDTO actualizar(
            Long calificacionId,
            CalificacionActualizarDTO dto,
            String correoUsuario
    ) {
        validarPuntuacion(dto.puntuacion());

        Usuario usuario = obtenerUsuario(correoUsuario);

        Calificacion calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Calificación no encontrada"
                ));

        if (!calificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar una calificación de otro usuario"
            );
        }

        obtenerEstablecimientoAprobado(
                calificacion.getEstablecimiento().getId()
        );

        calificacion.setPuntuacion(dto.puntuacion());

        return convertirRespuesta(calificacionRepository.save(calificacion));
    }

    @Override
    @Transactional
    public void eliminar(Long calificacionId, String correoUsuario) {

        Usuario usuario = obtenerUsuario(correoUsuario);

        Calificacion calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Calificación no encontrada"
                ));

        if (!calificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar una calificación de otro usuario"
            );
        }

        calificacionRepository.delete(calificacion);
    }

    private Usuario obtenerUsuario(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));
    }

    private Establecimiento obtenerEstablecimientoAprobado(Long id) {
        Establecimiento establecimiento = establecimientoRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Establecimiento no encontrado"
                ));

        if (establecimiento.getEstado() != EstadoEstablecimiento.APROBADO) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Establecimiento no disponible"
            );
        }

        return establecimiento;
    }

    private void validarPuntuacion(Integer puntuacion) {
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La puntuación debe estar entre 1 y 5"
            );
        }
    }

    private CalificacionRespuestaDTO convertirRespuesta(
            Calificacion calificacion
    ) {
        return new CalificacionRespuestaDTO(
                calificacion.getId(),
                calificacion.getPuntuacion(),
                calificacion.getUsuario().getId(),
                calificacion.getUsuario().getNombre(),
                calificacion.getEstablecimiento().getId()
        );
    }
}
