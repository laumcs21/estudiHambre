package com.estudiHambre.dto.beneficio;

import com.estudiHambre.model.TipoBeneficioEstudiantil;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BeneficioEstudiantilRespuestaDTO(
        Long id,
        TipoBeneficioEstudiantil tipo,
        String titulo,
        String descripcion,
        Integer porcentajeDescuento,
        Integer cantidadComidas,
        BigDecimal precioTiquetera,
        String codigo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activo,
        boolean vigente,
        Long establecimientoId
) {
}
