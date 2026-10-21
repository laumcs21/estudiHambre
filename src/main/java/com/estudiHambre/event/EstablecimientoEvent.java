
package com.estudiHambre.event;

public record EstablecimientoEvent(
        Long establecimientoId,
        Long propietarioId,
        String nombreEstablecimiento,
        TipoEvento tipo
) {

    public enum TipoEvento {
        CREADO,
        APROBADO,
        RECHAZADO
    }
}
