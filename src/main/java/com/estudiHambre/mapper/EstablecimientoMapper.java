package com.estudiHambre.mapper;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.model.Establecimiento;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class EstablecimientoMapper {

    public Establecimiento toEntity(EstablecimientoCrearDTO dto) {
        Establecimiento establecimiento = new Establecimiento();

        establecimiento.setNombre(dto.getNombre());
        establecimiento.setTipo(dto.getTipo());
        establecimiento.setDescripcion(dto.getDescripcion());
        establecimiento.setDireccion(dto.getDireccion());
        establecimiento.setTelefono(dto.getTelefono());
        establecimiento.setLatitud(dto.getLatitud());
        establecimiento.setLongitud(dto.getLongitud());
        establecimiento.setHoraApertura(dto.getHoraApertura());
        establecimiento.setHoraCierre(dto.getHoraCierre());
        establecimiento.setCerradoTemporalmente(Boolean.TRUE.equals(dto.getCerradoTemporalmente()));

        return establecimiento;
    }

    public EstablecimientoRespuestaDTO toDTO(Establecimiento establecimiento) {
        EstablecimientoRespuestaDTO dto = new EstablecimientoRespuestaDTO();

        dto.setId(establecimiento.getId());
        dto.setNombre(establecimiento.getNombre());
        dto.setTipo(establecimiento.getTipo());
        dto.setDescripcion(establecimiento.getDescripcion());
        dto.setDireccion(establecimiento.getDireccion());
        dto.setTelefono(establecimiento.getTelefono());
        dto.setImagenUrl(establecimiento.getImagenUrl());
        dto.setLatitud(establecimiento.getLatitud());
        dto.setLongitud(establecimiento.getLongitud());
        dto.setHoraApertura(establecimiento.getHoraApertura());
        dto.setHoraCierre(establecimiento.getHoraCierre());
        dto.setCerradoTemporalmente(Boolean.TRUE.equals(establecimiento.getCerradoTemporalmente()));
        dto.setAbiertoAhora(calcularAbiertoAhora(establecimiento));
        dto.setEstado(establecimiento.getEstado());

        return dto;
    }

    public void actualizarEntidad(
            EstablecimientoActualizarDTO dto,
            Establecimiento establecimiento) {

        if (dto.getNombre() != null) {
            establecimiento.setNombre(dto.getNombre());
        }

        if (dto.getTipo() != null) {
            establecimiento.setTipo(dto.getTipo());
        }

        if (dto.getDescripcion() != null) {
            establecimiento.setDescripcion(dto.getDescripcion());
        }

        if (dto.getDireccion() != null) {
            establecimiento.setDireccion(dto.getDireccion());
        }

        if (dto.getTelefono() != null) {
            establecimiento.setTelefono(dto.getTelefono());
        }

        if (dto.getLatitud() != null) {
            establecimiento.setLatitud(dto.getLatitud());
        }

        if (dto.getLongitud() != null) {
            establecimiento.setLongitud(dto.getLongitud());
        }

        if (dto.getHoraApertura() != null) {
            establecimiento.setHoraApertura(dto.getHoraApertura());
        }

        if (dto.getHoraCierre() != null) {
            establecimiento.setHoraCierre(dto.getHoraCierre());
        }

        if (dto.getCerradoTemporalmente() != null) {
            establecimiento.setCerradoTemporalmente(dto.getCerradoTemporalmente());
        }
    }
    private Boolean calcularAbiertoAhora(Establecimiento establecimiento) {
        if (Boolean.TRUE.equals(establecimiento.getCerradoTemporalmente())) {
            return false;
        }

        LocalTime apertura = establecimiento.getHoraApertura();
        LocalTime cierre = establecimiento.getHoraCierre();

        if (apertura == null || cierre == null) {
            return null;
        }

        LocalTime ahora = LocalTime.now(ZoneId.of("America/Bogota"));

        if (apertura.equals(cierre)) {
            return true;
        }

        if (apertura.isBefore(cierre)) {
            return !ahora.isBefore(apertura) && ahora.isBefore(cierre);
        }

        return !ahora.isBefore(apertura) || ahora.isBefore(cierre);
    }

}