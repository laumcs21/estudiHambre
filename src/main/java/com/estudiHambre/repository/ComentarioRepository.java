package com.estudiHambre.repository;

import com.estudiHambre.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository
        extends JpaRepository<Comentario, Long> {

    List<Comentario> findByEstablecimientoId(Long establecimientoId);

    List<Comentario> findByUsuarioId(Long usuarioId);
}