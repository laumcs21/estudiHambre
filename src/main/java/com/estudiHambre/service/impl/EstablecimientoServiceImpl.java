package com.estudiHambre.service.impl;

import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.event.EstablecimientoEvent;
import com.estudiHambre.exception.RecursoNoEncontradoException;
import com.estudiHambre.exception.ReglaNegocioException;
import com.estudiHambre.mapper.EstablecimientoMapper;
import com.estudiHambre.model.*;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.estudiHambre.service.EstablecimientoService;
import com.estudiHambre.service.CloudinaryService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class EstablecimientoServiceImpl implements EstablecimientoService {

    private final EstablecimientoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final EstablecimientoMapper mapper;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher eventPublisher;


    public EstablecimientoServiceImpl(
            EstablecimientoRepository repository,
            UsuarioRepository usuarioRepository,
            EstablecimientoMapper mapper,
            CloudinaryService cloudinaryService,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.cloudinaryService = cloudinaryService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarTodos() {

        return repository.findByEstado(EstadoEstablecimiento.APROBADO)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstablecimientoRespuestaDTO buscarPorId(Long id) {

        Establecimiento establecimiento = repository.findById(id)
                .filter(e -> e.getEstado() == EstadoEstablecimiento.APROBADO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Establecimiento no encontrado con ID: " + id
                ));

        return mapper.toDTO(establecimiento);
    }

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarMisEstablecimientos() {

        Usuario usuario = obtenerUsuarioAutenticado();

        return repository.findByPropietarioId(usuario.getId())
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
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
        validarHorario(dto.getHoraApertura(), dto.getHoraCierre());

        Establecimiento establecimiento = mapper.toEntity(dto);

        establecimiento.setPropietario(usuario);
        establecimiento.setEstado(EstadoEstablecimiento.PENDIENTE);
        establecimiento.setNombre(dto.getNombre().trim());

        CloudinaryService.ImagenSubida imagenSubida =
                cloudinaryService.subirImagen(imagen);

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

    @Override
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

        java.time.LocalTime aperturaFinal = dto.getHoraApertura() != null
                ? dto.getHoraApertura()
                : establecimiento.getHoraApertura();
        java.time.LocalTime cierreFinal = dto.getHoraCierre() != null
                ? dto.getHoraCierre()
                : establecimiento.getHoraCierre();
        validarHorario(aperturaFinal, cierreFinal);

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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarPendientes() {

        validarAdministrador();

        return repository.findByEstado(EstadoEstablecimiento.PENDIENTE)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
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

    @Override
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
                    .getLogger(EstablecimientoServiceImpl.class)
                    .warn("No se pudo limpiar la imagen de Cloudinary: {}",
                            publicId, e);
        }
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> listarTodosAdmin() {

        validarAdministrador();

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> buscarCercanos(
            Double latitud,
            Double longitud,
            Double radioKm) {

        if (latitud == null || longitud == null || radioKm == null) {
            throw new ReglaNegocioException(
                    "La latitud, longitud y el radio son obligatorios"
            );
        }

        if (!Double.isFinite(latitud)
                || !Double.isFinite(longitud)
                || latitud < -90 || latitud > 90
                || longitud < -180 || longitud > 180) {
            throw new ReglaNegocioException(
                    "Las coordenadas proporcionadas no son válidas"
            );
        }

        if (!Double.isFinite(radioKm) || radioKm <= 0) {
            throw new ReglaNegocioException(
                    "El radio debe ser mayor que cero"
            );
        }

        return repository
                .findByEstadoAndLatitudIsNotNullAndLongitudIsNotNull(
                        EstadoEstablecimiento.APROBADO
                )
                .stream()
                .filter(e -> Double.isFinite(e.getLatitud())
                        && Double.isFinite(e.getLongitud())
                        && e.getLatitud() >= -90
                        && e.getLatitud() <= 90
                        && e.getLongitud() >= -180
                        && e.getLongitud() <= 180)
                .filter(e -> calcularDistanciaKm(
                        latitud, longitud,
                        e.getLatitud(), e.getLongitud()
                ) <= radioKm)
                .sorted(Comparator.comparingDouble(e ->
                        calcularDistanciaKm(
                                latitud, longitud,
                                e.getLatitud(), e.getLongitud()
                        )
                ))
                .map(mapper::toDTO)
                .toList();
    }

    private double calcularDistanciaKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final double RADIO_TIERRA_KM = 6371.0;

        double diferenciaLatitud =
                Math.toRadians(lat2 - lat1);

        double diferenciaLongitud =
                Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(diferenciaLatitud / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(diferenciaLongitud / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(Math.max(0, 1 - a))
        );

        return RADIO_TIERRA_KM * c;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoRespuestaDTO> filtrarEstablecimientos(
            String nombre,
            String tipo,
            String plato,
            BigDecimal precioMax,
            Double latitud,
            Double longitud,
            Double radioKm) {

        // Validar el precio máximo
        if (precioMax != null && precioMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException(
                    "El precio máximo no puede ser negativo"
            );
        }

        // Normalizar los filtros de texto
        String nombreNormalizado = nombre == null
                ? ""
                : nombre.trim().toLowerCase(Locale.ROOT);

        String tipoNormalizado = tipo == null
                ? ""
                : tipo.trim().toLowerCase(Locale.ROOT);

        String platoNormalizado = plato == null
                ? ""
                : plato.trim().toLowerCase(Locale.ROOT);

        // Comprobar si se está utilizando el filtro de cercanía
        boolean filtrarPorDistancia =
                latitud != null || longitud != null || radioKm != null;

        if (filtrarPorDistancia) {

            if (latitud == null || longitud == null || radioKm == null) {
                throw new ReglaNegocioException(
                        "Para filtrar por cercanía debes proporcionar latitud, longitud y radioKm"
                );
            }

            if (!Double.isFinite(latitud)
                    || !Double.isFinite(longitud)
                    || latitud < -90 || latitud > 90
                    || longitud < -180 || longitud > 180) {
                throw new ReglaNegocioException(
                        "Las coordenadas proporcionadas no son válidas"
                );
            }

            if (!Double.isFinite(radioKm) || radioKm <= 0) {
                throw new ReglaNegocioException(
                        "El radio debe ser mayor que cero"
                );
            }
        }

        // Obtener únicamente establecimientos aprobados
        List<Establecimiento> establecimientos =
                repository.findByEstado(EstadoEstablecimiento.APROBADO);

        // Aplicar los filtros de forma combinada
        java.util.stream.Stream<Establecimiento> resultados =
                establecimientos.stream()

                        // Filtrar por nombre del establecimiento
                        .filter(e -> e.getNombre() != null
                                && e.getNombre()
                                .toLowerCase(Locale.ROOT)
                                .contains(nombreNormalizado))

                        // Filtrar por tipo de establecimiento
                        .filter(e -> tipoNormalizado.isEmpty()
                                || (e.getTipo() != null
                                && e.getTipo()
                                .toLowerCase(Locale.ROOT)
                                .equals(tipoNormalizado)))

                        // Filtrar por características de los platos
                        .filter(e -> {

                            if (platoNormalizado.isEmpty() && precioMax == null) {
                                return true;
                            }

                            return e.getMenus().stream().anyMatch(menu -> {

                                boolean coincidePlato =
                                        platoNormalizado.isEmpty()
                                                || (menu.getTitulo() != null
                                                && menu.getTitulo()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(platoNormalizado))
                                                || (menu.getDescripcion() != null
                                                && menu.getDescripcion()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(platoNormalizado));

                                boolean coincidePrecio =
                                        precioMax == null
                                                || (menu.getPrecio() != null
                                                && menu.getPrecio().compareTo(precioMax) <= 0);

                                return coincidePlato && coincidePrecio;
                            });
                        });

        // Aplicar el filtro de cercanía cuando se solicite
        if (filtrarPorDistancia) {

            resultados = resultados
                    .filter(e -> e.getLatitud() != null
                            && e.getLongitud() != null
                            && Double.isFinite(e.getLatitud())
                            && Double.isFinite(e.getLongitud())
                            && e.getLatitud() >= -90
                            && e.getLatitud() <= 90
                            && e.getLongitud() >= -180
                            && e.getLongitud() <= 180)
                    .filter(e -> calcularDistanciaKm(
                            latitud,
                            longitud,
                            e.getLatitud(),
                            e.getLongitud()
                    ) <= radioKm)
                    .sorted(Comparator.comparingDouble(
                            e -> calcularDistanciaKm(
                                    latitud,
                                    longitud,
                                    e.getLatitud(),
                                    e.getLongitud()
                            )
                    ));
        }

        return resultados
                .map(mapper::toDTO)
                .toList();
    }

    private void validarHorario(
            java.time.LocalTime horaApertura,
            java.time.LocalTime horaCierre
    ) {
        if ((horaApertura == null) != (horaCierre == null)) {
            throw new ReglaNegocioException(
                    "Debes indicar horaApertura y horaCierre juntas"
            );
        }
    }

}