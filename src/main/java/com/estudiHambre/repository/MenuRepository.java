
package com.estudiHambre.repository;

import com.estudiHambre.model.Menu;
import com.estudiHambre.model.EstadoEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByEstablecimientoId(Long establecimientoId);


    @Query("""

            SELECT m FROM Menu m
        JOIN m.establecimiento e
        WHERE e.estado = :estado
          AND (
              LOWER(m.titulo) LIKE CONCAT('%', LOWER(COALESCE(:nombre, '')), '%')
              OR LOWER(COALESCE(m.descripcion, '')) LIKE CONCAT('%', LOWER(COALESCE(:nombre, '')), '%')
          )
          AND m.precio <= COALESCE(:precioMax, m.precio)
          AND LOWER(e.nombre) LIKE CONCAT('%', LOWER(COALESCE(:nombreEstablecimiento, '')), '%')
          AND LOWER(COALESCE(e.tipo, '')) LIKE CONCAT('%', LOWER(COALESCE(:tipo, '')), '%')
        """)
    List<Menu> filtrarPlatos(
            @Param("estado") EstadoEstablecimiento estado,
            @Param("nombre") String nombre,
            @Param("precioMax") BigDecimal precioMax,
            @Param("nombreEstablecimiento") String nombreEstablecimiento,
            @Param("tipo") String tipo
    );
    }

