
package com.estudiHambre.repository;

import com.estudiHambre.model.Comentario;
import com.estudiHambre.model.EstadoComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository
        extends JpaRepository<Comentario, Long> {

    // Comentarios públicos de un establecimiento
    List<Comentario> findByEstablecimientoIdAndEstadoOrderByFechaDesc(
            Long establecimientoId,
            EstadoComentario estado
    );

    // Todos los comentarios de un establecimiento para moderación
    List<Comentario> findByEstablecimientoIdOrderByFechaDesc(
            Long establecimientoId
    );
}
