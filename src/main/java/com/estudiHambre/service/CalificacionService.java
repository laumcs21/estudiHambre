
package com.estudiHambre.service;

import com.estudiHambre.model.Calificacion;

import java.util.List;
import java.util.Optional;

public interface CalificacionService {

    List<Calificacion> listarTodos();

    Optional<Calificacion> buscarPorId(Long id);

    List<Calificacion> buscarPorEstablecimiento(Long establecimientoId);

    Calificacion guardar(Calificacion calificacion);

    void eliminar(Long id);
}
