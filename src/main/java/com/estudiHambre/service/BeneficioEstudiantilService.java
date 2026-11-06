package com.estudiHambre.service;

import com.estudiHambre.dto.beneficio.BeneficioEstudiantilActualizarDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilCrearDTO;
import com.estudiHambre.dto.beneficio.BeneficioEstudiantilRespuestaDTO;

import java.util.List;

public interface BeneficioEstudiantilService {

    List<BeneficioEstudiantilRespuestaDTO> listarPublicos(Long establecimientoId);

    List<BeneficioEstudiantilRespuestaDTO> listarParaGestion(Long establecimientoId);

    BeneficioEstudiantilRespuestaDTO crear(
            Long establecimientoId,
            BeneficioEstudiantilCrearDTO dto
    );

    BeneficioEstudiantilRespuestaDTO actualizar(
            Long beneficioId,
            BeneficioEstudiantilActualizarDTO dto
    );

    void eliminar(Long beneficioId);
}
