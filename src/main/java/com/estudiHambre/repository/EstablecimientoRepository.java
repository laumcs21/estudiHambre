package com.estudiHambre.repository;

import com.estudiHambre.model.Establecimiento;
import com.estudiHambre.model.EstadoEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstablecimientoRepository
        extends JpaRepository<Establecimiento, Long> {

    List<Establecimiento> findByEstado(EstadoEstablecimiento estado);

    List<Establecimiento> findByNombreContainingIgnoreCaseAndEstado(
            String nombre,
            EstadoEstablecimiento estado
    );

    List<Establecimiento> findByTipoIgnoreCaseAndEstado(
            String tipo,
            EstadoEstablecimiento estado
    );
    List<Establecimiento> findByPropietarioId(Long id);

    boolean existsByPropietarioIdAndEstado(
            Long propietarioId,
            EstadoEstablecimiento estado
    );

}