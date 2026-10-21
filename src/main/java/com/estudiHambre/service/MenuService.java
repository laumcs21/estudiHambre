package com.estudiHambre.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
public class MenuService {

    private static final Logger logger =
            LoggerFactory.getLogger(MenuService.class);

    private static final int MAX_IMAGENES = 5;

    private final MenuRepository menuRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MenuMapper menuMapper;
    private final CloudinaryService cloudinaryService;

    public MenuService(
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

    @Transactional(readOnly = true)
    public List<MenuRespuestaDTO> listarTodos() {

        return menuRepository.findAll()
                .stream()
                .filter(menu -> esPublico(menu.getEstablecimiento()))
                .map(menuMapper::toDTO)
                .toList();
    }

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
}