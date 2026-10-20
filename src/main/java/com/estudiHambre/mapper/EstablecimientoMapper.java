package com.estudiHambre.mapper;

import com.estudiHambre.dto.establecimiento.EstablecimientoCrearDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoActualizarDTO;
import com.estudiHambre.dto.establecimiento.EstablecimientoRespuestaDTO;
import com.estudiHambre.model.Establecimiento;
import org.springframework.stereotype.Component;

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

        if (dto.getImagenUrl() != null) {
            establecimiento.setImagenUrl(dto.getImagenUrl());
        }

        if (dto.getLatitud() != null) {
            establecimiento.setLatitud(dto.getLatitud());
        }

        if (dto.getLongitud() != null) {
            establecimiento.setLongitud(dto.getLongitud());
        }
    }
}