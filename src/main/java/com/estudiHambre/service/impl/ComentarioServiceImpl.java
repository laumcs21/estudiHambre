
package com.estudiHambre.service.impl;

import com.estudiHambre.dto.comentario.ComentarioActualizarDTO;
import com.estudiHambre.dto.comentario.ComentarioCrearDTO;
import com.estudiHambre.dto.comentario.ComentarioRespuestaDTO;
import com.estudiHambre.model.Comentario;
import com.estudiHambre.model.Establecimiento;
import com.estudiHambre.model.EstadoComentario;
import com.estudiHambre.model.EstadoEstablecimiento;
import com.estudiHambre.model.Usuario;
import com.estudiHambre.repository.ComentarioRepository;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.UsuarioRepository;
import com.estudiHambre.service.ComentarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioServiceImpl(
            ComentarioRepository comentarioRepository,
            EstablecimientoRepository establecimientoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.comentarioRepository = comentarioRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public ComentarioRespuestaDTO crear(
            Long establecimientoId,
            ComentarioCrearDTO dto,
            String correoUsuario
    ) {
        validarContenido(dto.contenido());

        Usuario usuario = obtenerUsuario(correoUsuario);
        Establecimiento establecimiento =
                obtenerEstablecimientoAprobado(establecimientoId);

        Comentario comentario = new Comentario();
        comentario.setContenido(dto.contenido().trim());
        comentario.setUsuario(usuario);
        comentario.setEstablecimiento(establecimiento);
        comentario.setEstado(EstadoComentario.VISIBLE);

        Comentario guardado = comentarioRepository.save(comentario);

        return convertirRespuesta(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioRespuestaDTO> listarVisibles(
            Long establecimientoId
    ) {
        obtenerEstablecimientoAprobado(establecimientoId);

        return comentarioRepository
                .findByEstablecimientoIdAndEstadoOrderByFechaDesc(
                        establecimientoId,
                        EstadoComentario.VISIBLE
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public ComentarioRespuestaDTO actualizar(
            Long comentarioId,
            ComentarioActualizarDTO dto,
            String correoUsuario
    ) {
        validarContenido(dto.contenido());

        Usuario usuario = obtenerUsuario(correoUsuario);
        Comentario comentario = obtenerComentario(comentarioId);

        verificarPropietario(comentario, usuario);

        obtenerEstablecimientoAprobado(
                comentario.getEstablecimiento().getId()
        );

        // Un comentario oculto no puede editarse para evadir la moderación
        if (comentario.getEstado() == EstadoComentario.OCULTO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes editar un comentario oculto por moderación"
            );
        }

        comentario.setContenido(dto.contenido().trim());

        return convertirRespuesta(comentarioRepository.save(comentario));
    }

    @Override
    @Transactional
    public void eliminar(Long comentarioId, String correoUsuario) {

        Usuario usuario = obtenerUsuario(correoUsuario);
        Comentario comentario = obtenerComentario(comentarioId);

        verificarPropietario(comentario, usuario);

        comentarioRepository.delete(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioRespuestaDTO> listarTodosParaAdministracion(
            Long establecimientoId
    ) {
        obtenerEstablecimiento(establecimientoId);

        return comentarioRepository
                .findByEstablecimientoIdOrderByFechaDesc(establecimientoId)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public ComentarioRespuestaDTO ocultar(Long comentarioId) {

        Comentario comentario = obtenerComentario(comentarioId);

        if (comentario.getEstado() == EstadoComentario.OCULTO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El comentario ya está oculto"
            );
        }

        comentario.setEstado(EstadoComentario.OCULTO);

        return convertirRespuesta(comentarioRepository.save(comentario));
    }

    @Override
    @Transactional
    public ComentarioRespuestaDTO restaurar(Long comentarioId) {

        Comentario comentario = obtenerComentario(comentarioId);

        if (comentario.getEstado() == EstadoComentario.VISIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El comentario ya está visible"
            );
        }

        comentario.setEstado(EstadoComentario.VISIBLE);

        return convertirRespuesta(comentarioRepository.save(comentario));
    }

    private Usuario obtenerUsuario(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));
    }

    private Establecimiento obtenerEstablecimiento(Long id) {
        return establecimientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Establecimiento no encontrado"
                ));
    }

    private Establecimiento obtenerEstablecimientoAprobado(Long id) {

        Establecimiento establecimiento = obtenerEstablecimiento(id);

        if (establecimiento.getEstado() != EstadoEstablecimiento.APROBADO) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Establecimiento no disponible"
            );
        }

        return establecimiento;
    }

    private Comentario obtenerComentario(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comentario no encontrado"
                ));
    }

    private void verificarPropietario(
            Comentario comentario,
            Usuario usuario
    ) {
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar comentarios de otro usuario"
            );
        }
    }

    private void validarContenido(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El comentario no puede estar vacío"
            );
        }

        if (contenido.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El comentario no puede superar los 1000 caracteres"
            );
        }
    }

    private ComentarioRespuestaDTO convertirRespuesta(Comentario comentario) {
        return new ComentarioRespuestaDTO(
                comentario.getId(),
                comentario.getContenido(),
                comentario.getFecha(),
                comentario.getEstado(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNombre(),
                comentario.getEstablecimiento().getId()
        );
    }
}
