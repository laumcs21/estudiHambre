package com.estudiHambre.repository;

import com.estudiHambre.model.BeneficioEstudiantil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficioEstudiantilRepository
        extends JpaRepository<BeneficioEstudiantil, Long> {

    List<BeneficioEstudiantil> findByEstablecimientoIdOrderByIdDesc(Long establecimientoId);
}
