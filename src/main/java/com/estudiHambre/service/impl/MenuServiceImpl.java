package com.estudiHambre.service.impl;

import com.estudiHambre.dto.menu.MenuActualizarDTO;
import com.estudiHambre.dto.menu.MenuCrearDTO;
import com.estudiHambre.dto.menu.MenuRespuestaDTO;
import com.estudiHambre.exception.RecursoNoEncontradoException;
import com.estudiHambre.exception.ReglaNegocioException;
import com.estudiHambre.mapper.MenuMapper;
import com.estudiHambre.model.*;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.MenuRepository;
import com.estudiHambre.repository.UsuarioRepository;
import com.estudiHambre.service.MenuService;
import com.estudiHambre.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger logger =
            LoggerFactory.getLogger(MenuServiceImpl.class);

    private static final int MAX_IMAGENES = 5;

    private final MenuRepository menuRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MenuMapper menuMapper;
    private final CloudinaryService cloudinaryService;

    public MenuServiceImpl(
            MenuRepository menuRepository,
            EstablecimientoRepository establecimientoRepository,
            UsuarioRepository usuarioRepository,
            MenuMapper menuMapper,
            CloudinaryService cloudinaryService) {

        this.menuRepository = menuRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.menuMapper = menuMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuRespuestaDTO> listarTodos() {

        return menuRepository.findAll()
                .stream()
                .filter(menu -> esPublico(menu.getEstablecimiento()))
                .map(menuMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuRespuestaDTO buscarPorId(Long id) {

        Menu menu = obtenerMenu(id);

        if (!esPublico(menu.getEstablecimiento())) {
            throw new RecursoNoEncontradoException(
                    "Menú no encontrado"
            );
        }

        return menuMapper.toDTO(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuRespuestaDTO> buscarPorEstablecimiento(Long establecimientoId) {

        Establecimiento establecimiento =
                obtenerEstablecimiento(establecimientoId);

        if (!esPublico(establecimiento)) {
            throw new RecursoNoEncontradoException(
                    "Establecimiento no encontrado"
            );
        }

        return menuRepository.findByEstablecimientoId(establecimientoId)
                .stream()
                .map(menuMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public MenuRespuestaDTO crear(
            MenuCrearDTO dto,
            List<MultipartFile> imagenes) {

        Usuario usuario = obtenerUsuarioAutenticado();

        Establecimiento establecimiento =
                obtenerEstablecimiento(dto.getEstablecimientoId());

        validarPermisos(establecimiento, usuario);

        if (establecimiento.getEstado() != EstadoEstablecimiento.APROBADO) {
            throw new ReglaNegocioException(
                    "Solo puedes agregar menús a establecimientos aprobados"
            );
        }

        validarCantidadImagenes(imagenes, 0);

        Menu menu = menuMapper.toEntity(dto);
        menu.setEstablecimiento(establecimiento);

        subirImagenes(menu, imagenes);

        Menu guardado = menuRepository.saveAndFlush(menu);

        return menuMapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public MenuRespuestaDTO actualizar(
            Long id,
            MenuActualizarDTO dto,
            List<MultipartFile> imagenes) {

        Usuario usuario = obtenerUsuarioAutenticado();
        Menu menu = obtenerMenu(id);

        validarPermisos(menu.getEstablecimiento(), usuario);

        menuMapper.actualizarEntidad(dto, menu);

        validarCantidadImagenes(imagenes, menu.getImagenes().size());

        subirImagenes(menu, imagenes);

        Menu guardado = menuRepository.saveAndFlush(menu);

        return menuMapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Usuario usuario = obtenerUsuarioAutenticado();
        Menu menu = obtenerMenu(id);

        validarPermisos(menu.getEstablecimiento(), usuario);

        List<String> publicIds = menu.getImagenes()
                .stream()
                .map(MenuImagen::getPublicId)
                .filter(publicId -> publicId != null && !publicId.isBlank())
                .toList();

        menuRepository.delete(menu);
        menuRepository.flush();

        for (String publicId : publicIds) {
            eliminarImagenDespuesDeCommit(publicId);
        }
    }

    private void subirImagenes(
            Menu menu,
            List<MultipartFile> imagenes) {

        if (imagenes == null || imagenes.isEmpty()) {
            return;
        }

        for (MultipartFile archivo : imagenes) {

            CloudinaryService.ImagenSubida subida =
                    cloudinaryService.subirImagen(
                            archivo,
                            "estudihambre/menus"
                    );

            limpiarImagenSiHayRollback(subida.publicId());

            menu.getImagenes().add(
                    new MenuImagen(
                            subida.url(),
                            subida.publicId()
                    )
            );
        }
    }

    private void validarCantidadImagenes(
            List<MultipartFile> imagenes,
            int cantidadActual) {

        int nuevas = imagenes == null ? 0 : imagenes.size();

        if (cantidadActual + nuevas > MAX_IMAGENES) {
            throw new ReglaNegocioException(
                    "Cada menú puede tener máximo 5 imágenes"
            );
        }
    }

    private boolean esPublico(Establecimiento establecimiento) {
        return establecimiento.getEstado()
                == EstadoEstablecimiento.APROBADO;
    }

    private Menu obtenerMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Menú no encontrado"
                        )
                );
    }

    private Establecimiento obtenerEstablecimiento(Long id) {
        return establecimientoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Establecimiento no encontrado"
                        )
                );
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

        return usuarioRepository.findByCorreo(authentication.getName())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario autenticado no encontrado"
                        )
                );
    }



    private void validarPermisos(
            Establecimiento establecimiento,
            Usuario usuario) {

        boolean esAdministrador = usuario.getRol() == Rol.ADMIN;

        boolean esPropietario =
                establecimiento.getPropietario() != null
                        && establecimiento.getPropietario().getId()
                        .equals(usuario.getId());

        if (!esAdministrador && !esPropietario) {
            throw new AccessDeniedException(
                    "No tienes permisos para gestionar este menú"
            );
        }
    }


    private void limpiarImagenSiHayRollback(String publicId) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_COMMITTED) {
                            intentarEliminarImagen(publicId);
                        }
                    }
                }
        );
    }

    private void eliminarImagenDespuesDeCommit(String publicId) {

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

        try {
            cloudinaryService.eliminarImagen(publicId);
        } catch (Exception e) {
            logger.warn(
                    "No fue posible eliminar la imagen de Cloudinary: {}",
                    publicId,
                    e
            );
        }
    }

    @Override
    @Transactional
    public void eliminarImagen(Long menuId, int indice) {

        Usuario usuario = obtenerUsuarioAutenticado();
        Menu menu = obtenerMenu(menuId);

        validarPermisos(menu.getEstablecimiento(), usuario);

        if (indice < 0 || indice >= menu.getImagenes().size()) {
            throw new RecursoNoEncontradoException(
                    "Imagen no encontrada"
            );
        }

        MenuImagen imagen = menu.getImagenes().remove(indice);

        menuRepository.saveAndFlush(menu);

        eliminarImagenDespuesDeCommit(imagen.getPublicId());
    }


    @Override
    @Transactional(readOnly = true)
    public List<MenuRespuestaDTO> filtrarPlatos(
            String nombre,
            BigDecimal precioMax,
            String orden,
            String nombreEstablecimiento,
            String tipo,
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
        String nombreNormalizado =
                nombre == null || nombre.isBlank()
                        ? null
                        : nombre.trim();

        String establecimientoNormalizado =
                nombreEstablecimiento == null || nombreEstablecimiento.isBlank()
                        ? null
                        : nombreEstablecimiento.trim();

        String tipoNormalizado =
                tipo == null || tipo.isBlank()
                        ? null
                        : tipo.trim();

        // Validar el orden por precio
        String ordenNormalizado =
                orden == null || orden.isBlank()
                        ? "asc"
                        : orden.trim().toLowerCase(Locale.ROOT);

        if (!ordenNormalizado.equals("asc")
                && !ordenNormalizado.equals("desc")) {
            throw new ReglaNegocioException(
                    "El orden debe ser asc o desc"
            );
        }

        // Validar si se solicita el filtro de cercanía
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

        // Obtener los platos que cumplen los filtros de base de datos
        List<Menu> platos = menuRepository.filtrarPlatos(
                EstadoEstablecimiento.APROBADO,
                nombreNormalizado,
                precioMax,
                establecimientoNormalizado,
                tipoNormalizado
        );

        // Configurar el ordenamiento por precio
        Comparator<Menu> comparador =
                Comparator.comparing(Menu::getPrecio);

        if (ordenNormalizado.equals("desc")) {
            comparador = comparador.reversed();
        }

        final boolean aplicarDistancia = filtrarPorDistancia;

        // Combinar todos los filtros y devolver los resultados
        return platos.stream()
                .filter(menu -> {

                    if (!aplicarDistancia) {
                        return true;
                    }

                    Establecimiento establecimiento =
                            menu.getEstablecimiento();

                    Double latEstablecimiento =
                            establecimiento.getLatitud();

                    Double lonEstablecimiento =
                            establecimiento.getLongitud();

                    if (latEstablecimiento == null
                            || lonEstablecimiento == null
                            || !Double.isFinite(latEstablecimiento)
                            || !Double.isFinite(lonEstablecimiento)
                            || latEstablecimiento < -90
                            || latEstablecimiento > 90
                            || lonEstablecimiento < -180
                            || lonEstablecimiento > 180) {
                        return false;
                    }

                    double distancia = calcularDistanciaKm(
                            latitud,
                            longitud,
                            latEstablecimiento,
                            lonEstablecimiento
                    );

                    return distancia <= radioKm;
                })
                .sorted(comparador.thenComparing(Menu::getId))
                .map(menuMapper::toDTO)
                .toList();
    }


    private double calcularDistanciaKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final double RADIO_TIERRA_KM = 6371.0;

        double diferenciaLatitud = Math.toRadians(lat2 - lat1);
        double diferenciaLongitud = Math.toRadians(lon2 - lon1);

        double a =
                Math.pow(Math.sin(diferenciaLatitud / 2), 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.pow(Math.sin(diferenciaLongitud / 2), 2);

        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return RADIO_TIERRA_KM * c;
    }

}
