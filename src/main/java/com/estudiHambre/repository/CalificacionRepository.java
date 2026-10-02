package com.estudiHambre.repository;

import com.estudiHambre.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository
        extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByEstablecimientoId(Long establecimientoId);

    List<Calificacion> findByUsuarioId(Long usuarioId);
}