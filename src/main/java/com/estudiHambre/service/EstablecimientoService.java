package com.estudiHambre.service;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.exception.RecursoNoEncontradoException;
import com.estudiHambre.exception.ReglaNegocioException;
import com.estudiHambre.mapper.EstablecimientoMapper;
import com.estudiHambre.model.*;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.UsuarioRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.estudiHambre.event.EstablecimientoEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EstablecimientoService {

    private final EstablecimientoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final EstablecimientoMapper mapper;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher eventPublisher;


    public EstablecimientoService(
            EstablecimientoRepository repository,
            UsuarioRepository usuarioRepository,
            EstablecimientoMapper mapper,
            CloudinaryService cloudinaryService, ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.cloudinaryService = cloudinaryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarTodos() {

        return repository.findByEstado(EstadoEstablecimiento.APROBADO)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstablecimientoRespuestaDTO buscarPorId(Long id) {

        Establecimiento establecimiento = repository.findById(id)
                .filter(e -> e.getEstado() == EstadoEstablecimiento.APROBADO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Establecimiento no encontrado con ID: " + id
                ));

        return mapper.toDTO(establecimiento);
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> buscarPorNombre(String nombre) {

        return repository.findByNombreContainingIgnoreCaseAndEstado(
                        nombre,
                        EstadoEstablecimiento.APROBADO
                )
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> buscarPorTipo(String tipo) {

        return repository.findByTipoIgnoreCaseAndEstado(
                        tipo,
                        EstadoEstablecimiento.APROBADO
                )
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarMisEstablecimientos() {

        Usuario usuario = obtenerUsuarioAutenticado();

        return repository.findByPropietarioId(usuario.getId())
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public EstablecimientoRespuestaDTO crear(
            EstablecimientoCrearDTO dto,
            MultipartFile imagen) {

        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario.getRol() == Rol.ADMIN) {
            throw new AccessDeniedException(
                    "Los administradores no pueden registrar establecimientos"
            );
        }
        validarNombre(dto.getNombre());

        Establecimiento establecimiento = mapper.toEntity(dto);

        establecimiento.setPropietario(usuario);
        establecimiento.setEstado(EstadoEstablecimiento.PENDIENTE);
        establecimiento.setNombre(dto.getNombre().trim());

        CloudinaryService.ImagenSubida imagenSubida =
                cloudinaryService.subirImagen(imagen);

        // Si falla la transacción, eliminamos la imagen recién subida.
        limpiarImagenSiHayRollback(imagenSubida.publicId());

        establecimiento.setImagenUrl(imagenSubida.url());
        establecimiento.setImagenPublicId(imagenSubida.publicId());

        Establecimiento guardado = repository.saveAndFlush(establecimiento);


        eventPublisher.publishEvent(
                new EstablecimientoEvent(
                        guardado.getId(),
                        usuario.getId(),
                        guardado.getNombre(),
                        EstablecimientoEvent.TipoEvento.CREADO
                )
        );


        return mapper.toDTO(guardado);
    }
    @Transactional
    public EstablecimientoRespuestaDTO actualizar(
            Long id,
            EstablecimientoActualizarDTO dto,
            MultipartFile imagen) {

        Usuario usuario = obtenerUsuarioAutenticado();
        Establecimiento establecimiento = obtenerEntidad(id);

        validarPropietario(establecimiento, usuario);

        if (dto.getNombre() != null) {
            validarNombre(dto.getNombre());
        }

        mapper.actualizarEntidad(dto, establecimiento);

        if (dto.getNombre() != null) {
            establecimiento.setNombre(dto.getNombre().trim());
        }

        if (imagen != null && !imagen.isEmpty()) {

            String imagenAnteriorId = establecimiento.getImagenPublicId();

            CloudinaryService.ImagenSubida nuevaImagen =
                    cloudinaryService.subirImagen(imagen);

            limpiarImagenSiHayRollback(nuevaImagen.publicId());

            establecimiento.setImagenUrl(nuevaImagen.url());
            establecimiento.setImagenPublicId(nuevaImagen.publicId());

            eliminarImagenDespuesDeCommit(imagenAnteriorId);
        }

        Establecimiento guardado = repository.saveAndFlush(establecimiento);

        return mapper.toDTO(guardado);
    }


    @Transactional
    public void eliminar(Long id) {

        Usuario usuario = obtenerUsuarioAutenticado();
        Establecimiento establecimiento = obtenerEntidad(id);

        validarPropietario(establecimiento, usuario);

        Usuario propietario = establecimiento.getPropietario();

        boolean estabaAprobado =
                establecimiento.getEstado() == EstadoEstablecimiento.APROBADO;

        // Guardar el identificador de la imagen principal.
        String imagenPublicId = establecimiento.getImagenPublicId();

        // Guardar los identificadores de las imágenes de todos los menús.
        List<String> imagenesMenus = establecimiento.getMenus()
                .stream()
                .flatMap(menu -> menu.getImagenes().stream())
                .map(MenuImagen::getPublicId)
                .filter(publicId -> publicId != null && !publicId.isBlank())
                .toList();

        // Eliminar el establecimiento y sus menús de PostgreSQL.
        repository.delete(establecimiento);
        repository.flush();

        // Actualizar el rol si era su último establecimiento aprobado.
        if (estabaAprobado && propietario != null
                && propietario.getRol() == Rol.RESTAURANTE) {

            boolean tieneAprobados =
                    repository.existsByPropietarioIdAndEstado(
                            propietario.getId(),
                            EstadoEstablecimiento.APROBADO
                    );

            if (!tieneAprobados) {
                propietario.setRol(Rol.ESTUDIANTE);
                usuarioRepository.save(propietario);
            }
        }

        // Eliminar la imagen principal después del commit.
        eliminarImagenDespuesDeCommit(imagenPublicId);

        // Eliminar las imágenes de los menús después del commit.
        for (String publicId : imagenesMenus) {
            eliminarImagenDespuesDeCommit(publicId);
        }
    }


    private Usuario obtenerUsuarioAutenticado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación"
            );
        }

        String correo = authentication.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado"
                ));
    }

    private void validarPropietario(
            Establecimiento establecimiento,
            Usuario usuario) {

        if (usuario.getRol() == Rol.ADMIN) {
            return;
        }

        if (establecimiento.getPropietario() == null
                || !establecimiento.getPropietario()
                .getId().equals(usuario.getId())) {

            throw new AccessDeniedException(
                    "No tienes permiso para modificar este establecimiento"
            );
        }
    }

    private Establecimiento obtenerEntidad(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Establecimiento no encontrado con ID: " + id
                ));
    }

    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException(
                    "El nombre del establecimiento es obligatorio"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarPendientes() {

        validarAdministrador();

        return repository.findByEstado(EstadoEstablecimiento.PENDIENTE)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public EstablecimientoRespuestaDTO aprobar(Long id) {

        validarAdministrador();

        Establecimiento establecimiento = obtenerEntidad(id);

        if (establecimiento.getEstado() != EstadoEstablecimiento.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se pueden aprobar establecimientos pendientes"
            );
        }

        Usuario propietario = establecimiento.getPropietario();

        if (propietario == null) {
            throw new ReglaNegocioException(
                    "El establecimiento no tiene un propietario asignado"
            );
        }

        establecimiento.setEstado(EstadoEstablecimiento.APROBADO);

        if (propietario.getRol() == Rol.ESTUDIANTE) {
            propietario.setRol(Rol.RESTAURANTE);
            usuarioRepository.save(propietario);
        }


        Establecimiento guardado = repository.save(establecimiento);

        eventPublisher.publishEvent(
                new EstablecimientoEvent(
                        guardado.getId(),
                        propietario.getId(),
                        guardado.getNombre(),
                        EstablecimientoEvent.TipoEvento.APROBADO
                )
        );

        return mapper.toDTO(guardado);
    }

    @Transactional
    public EstablecimientoRespuestaDTO rechazar(Long id) {

        validarAdministrador();

        Establecimiento establecimiento = obtenerEntidad(id);

        if (establecimiento.getEstado() != EstadoEstablecimiento.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se pueden rechazar establecimientos pendientes"
            );
        }

        establecimiento.setEstado(EstadoEstablecimiento.RECHAZADO);


        Establecimiento guardado = repository.save(establecimiento);

        eventPublisher.publishEvent(
                new EstablecimientoEvent(
                        guardado.getId(),
                        guardado.getPropietario().getId(),
                        guardado.getNombre(),
                        EstablecimientoEvent.TipoEvento.RECHAZADO
                )
        );

        return mapper.toDTO(guardado);
    }

    private void validarAdministrador() {

        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario.getRol() != Rol.ADMIN) {
            throw new AccessDeniedException(
                    "Solo un administrador puede realizar esta operación"
            );
        }
    }

    private void limpiarImagenSiHayRollback(String publicId) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Se requiere una transacción activa"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status == STATUS_ROLLED_BACK) {
                            intentarEliminarImagen(publicId);
                        }
                    }
                }
        );
    }

    private void eliminarImagenDespuesDeCommit(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Se requiere una transacción activa"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        intentarEliminarImagen(publicId);
                    }
                }
        );
    }

    private void intentarEliminarImagen(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinaryService.eliminarImagen(publicId);
        } catch (Exception e) {
            // La operación en PostgreSQL ya terminó.
            // Registramos el fallo sin invalidar la respuesta.
            org.slf4j.LoggerFactory
                    .getLogger(EstablecimientoService.class)
                    .warn("No se pudo limpiar la imagen de Cloudinary: {}",
                            publicId, e);
        }
    }


    @Transactional
    public void eliminarImagen(Long id) {

        Usuario usuario = obtenerUsuarioAutenticado();

        Establecimiento establecimiento = repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Establecimiento no encontrado"
                        )
                );

        validarPropietario(establecimiento, usuario);

        String publicIdAnterior = establecimiento.getImagenPublicId();

        if (publicIdAnterior == null || publicIdAnterior.isBlank()) {
            throw new ReglaNegocioException(
                    "El establecimiento no tiene una imagen para eliminar"
            );
        }

        establecimiento.setImagenUrl(null);
        establecimiento.setImagenPublicId(null);

        repository.saveAndFlush(establecimiento);

        eliminarImagenDespuesDeCommit(publicIdAnterior);
    }


    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarTodosAdmin() {

        validarAdministrador();

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }


}