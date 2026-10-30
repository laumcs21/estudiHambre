package com.estudiHambre.repository;

import com.estudiHambre.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository
        extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByEstablecimientoId(Long establecimientoId);

    List<Calificacion> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndEstablecimientoId(
            Long usuarioId,
            Long establecimientoId
    );


    long countByEstablecimientoId(Long establecimientoId);

    @Query("""
        SELECT AVG(c.puntuacion)
        FROM Calificacion c
        WHERE c.establecimiento.id = :establecimientoId
        """)
    Double calcularPromedio(
            @Param("establecimientoId") Long establecimientoId
    );
}