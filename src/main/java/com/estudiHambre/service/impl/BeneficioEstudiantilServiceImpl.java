package com.estudiHambre.service.impl;

import com.estudiHambre.dto.beneficio.BeneficioEstudiantilActualizarDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilCrearDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilRespuestaDTO;
import com.estudiHambre.exception.RecursoNoEncontradoException;
import com.estudiHambre.exception.ReglaNegocioException;
import com.estudiHambre.model.*;
import com.estudiHambre.repository.BeneficioEstudiantilRepository;
import com.estudiHambre.repository.EstablecimientoRepository;
import com.estudiHambre.repository.UsuarioRepository;
import com.estudiHambre.service.BeneficioEstudiantilService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class BeneficioEstudiantilServiceImpl implements BeneficioEstudiantilService {

    private final BeneficioEstudiantilRepository beneficioRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public BeneficioEstudiantilServiceImpl(
            BeneficioEstudiantilRepository beneficioRepository,
            EstablecimientoRepository establecimientoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.beneficioRepository = beneficioRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficioEstudiantilRespuestaDTO> listarPublicos(Long establecimientoId) {
        Establecimiento establecimiento = obtenerEstablecimiento(establecimientoId);

        if (establecimiento.getEstado() != EstadoEstablecimiento.APROBADO) {
            throw new RecursoNoEncontradoException("Establecimiento no encontrado");
        }

        return beneficioRepository
                .findByEstablecimientoIdOrderByIdDesc(establecimientoId)
                .stream()
                .filter(this::estaVigente)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficioEstudiantilRespuestaDTO> listarParaGestion(
            Long establecimientoId
    ) {
        Establecimiento establecimiento = obtenerEstablecimiento(establecimientoId);
        validarPermisos(establecimiento);

        return beneficioRepository
                .findByEstablecimientoIdOrderByIdDesc(establecimientoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public BeneficioEstudiantilRespuestaDTO crear(
            Long establecimientoId,
            BeneficioEstudiantilCrearDTO dto
    ) {
        Establecimiento establecimiento = obtenerEstablecimiento(establecimientoId);
        validarPermisos(establecimiento);
        validarDatos(
                dto.tipo(),
                dto.porcentajeDescuento(),
                dto.cantidadComidas(),
                dto.precioTiquetera(),
                dto.fechaInicio(),
                dto.fechaFin()
        );

        BeneficioEstudiantil beneficio = new BeneficioEstudiantil();
        beneficio.setEstablecimiento(establecimiento);
        beneficio.setTipo(dto.tipo());
        beneficio.setTitulo(dto.titulo().trim());
        beneficio.setDescripcion(limpiar(dto.descripcion()));
        beneficio.setPorcentajeDescuento(dto.porcentajeDescuento());
        beneficio.setCantidadComidas(dto.cantidadComidas());
        beneficio.setPrecioTiquetera(dto.precioTiquetera());
        beneficio.setCodigo(limpiar(dto.codigo()));
        beneficio.setFechaInicio(dto.fechaInicio());
        beneficio.setFechaFin(dto.fechaFin());
        beneficio.setActivo(dto.activo() == null || dto.activo());

        limpiarCamposSegunTipo(beneficio);

        return toDTO(beneficioRepository.save(beneficio));
    }

    @Override
    @Transactional
    public BeneficioEstudiantilRespuestaDTO actualizar(
            Long beneficioId,
            BeneficioEstudiantilActualizarDTO dto
    ) {
        BeneficioEstudiantil beneficio = obtenerBeneficio(beneficioId);
        validarPermisos(beneficio.getEstablecimiento());

        if (dto.tipo() != null) beneficio.setTipo(dto.tipo());
        if (dto.titulo() != null) beneficio.setTitulo(dto.titulo().trim());
        if (dto.descripcion() != null) beneficio.setDescripcion(limpiar(dto.descripcion()));
        if (dto.porcentajeDescuento() != null) beneficio.setPorcentajeDescuento(dto.porcentajeDescuento());
        if (dto.cantidadComidas() != null) beneficio.setCantidadComidas(dto.cantidadComidas());
        if (dto.precioTiquetera() != null) beneficio.setPrecioTiquetera(dto.precioTiquetera());
        if (dto.codigo() != null) beneficio.setCodigo(limpiar(dto.codigo()));
        if (dto.fechaInicio() != null) beneficio.setFechaInicio(dto.fechaInicio());
        if (dto.fechaFin() != null) beneficio.setFechaFin(dto.fechaFin());
        if (dto.activo() != null) beneficio.setActivo(dto.activo());

        validarDatos(
                beneficio.getTipo(),
                beneficio.getPorcentajeDescuento(),
                beneficio.getCantidadComidas(),
                beneficio.getPrecioTiquetera(),
                beneficio.getFechaInicio(),
                beneficio.getFechaFin()
        );

        limpiarCamposSegunTipo(beneficio);

        return toDTO(beneficioRepository.save(beneficio));
    }

    @Override
    @Transactional
    public void eliminar(Long beneficioId) {
        BeneficioEstudiantil beneficio = obtenerBeneficio(beneficioId);
        validarPermisos(beneficio.getEstablecimiento());
        beneficioRepository.delete(beneficio);
    }

    private void validarDatos(
            TipoBeneficioEstudiantil tipo,
            Integer porcentajeDescuento,
            Integer cantidadComidas,
            java.math.BigDecimal precioTiquetera,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        if (tipo == TipoBeneficioEstudiantil.DESCUENTO && porcentajeDescuento == null) {
            throw new ReglaNegocioException(
                    "Un descuento debe indicar porcentajeDescuento"
            );
        }

        if (tipo == TipoBeneficioEstudiantil.TIQUETERA
                && (cantidadComidas == null || precioTiquetera == null)) {
            throw new ReglaNegocioException(
                    "Una tiquetera debe indicar cantidadComidas y precioTiquetera"
            );
        }

        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void limpiarCamposSegunTipo(BeneficioEstudiantil beneficio) {
        if (beneficio.getTipo() == TipoBeneficioEstudiantil.DESCUENTO) {
            beneficio.setCantidadComidas(null);
            beneficio.setPrecioTiquetera(null);
        } else {
            beneficio.setPorcentajeDescuento(null);
        }
    }

    private boolean estaVigente(BeneficioEstudiantil beneficio) {
        if (!beneficio.isActivo()) return false;

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        boolean inicioValido = beneficio.getFechaInicio() == null
                || !hoy.isBefore(beneficio.getFechaInicio());
        boolean finValido = beneficio.getFechaFin() == null
                || !hoy.isAfter(beneficio.getFechaFin());

        return inicioValido && finValido;
    }

    private BeneficioEstudiantilRespuestaDTO toDTO(BeneficioEstudiantil beneficio) {
        return new BeneficioEstudiantilRespuestaDTO(
                beneficio.getId(),
                beneficio.getTipo(),
                beneficio.getTitulo(),
                beneficio.getDescripcion(),
                beneficio.getPorcentajeDescuento(),
                beneficio.getCantidadComidas(),
                beneficio.getPrecioTiquetera(),
                beneficio.getCodigo(),
                beneficio.getFechaInicio(),
                beneficio.getFechaFin(),
                beneficio.isActivo(),
                estaVigente(beneficio),
                beneficio.getEstablecimiento().getId()
        );
    }

    private Establecimiento obtenerEstablecimiento(Long id) {
        return establecimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Establecimiento no encontrado"
                ));
    }

    private BeneficioEstudiantil obtenerBeneficio(Long id) {
        return beneficioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Beneficio estudiantil no encontrado"
                ));
    }

    private void validarPermisos(Establecimiento establecimiento) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario.getRol() == Rol.ADMIN) return;

        if (establecimiento.getPropietario() == null
                || !establecimiento.getPropietario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException(
                    "No tienes permisos para gestionar beneficios de este establecimiento"
            );
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

        return usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado"
                ));
    }

    private String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
