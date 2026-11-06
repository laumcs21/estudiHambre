package com.estudiHambre.dto.beneficio;

import com.estudiHambre.model.TipoBeneficioEstudiantil;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BeneficioEstudiantilCrearDTO(
        @NotNull(message = "El tipo de beneficio es obligatorio")
        TipoBeneficioEstudiantil tipo,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @Min(value = 1, message = "El porcentaje de descuento debe ser al menos 1")
        @Max(value = 100, message = "El porcentaje de descuento no puede superar 100")
        Integer porcentajeDescuento,

        @Min(value = 2, message = "La tiquetera debe incluir al menos 2 comidas")
        Integer cantidadComidas,

        @DecimalMin(value = "0.01", message = "El precio de la tiquetera debe ser mayor que cero")
        BigDecimal precioTiquetera,

        @Size(max = 80, message = "El código no puede superar los 80 caracteres")
        String codigo,

        LocalDate fechaInicio,
        LocalDate fechaFin,
        Boolean activo
) {
}
