
package com.estudiHambre.service.impl;

import com.estudiHambre.model.Calificacion;
import com.estudiHambre.repository.CalificacionRepository;
import com.estudiHambre.service.CalificacionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;

    public CalificacionServiceImpl(CalificacionRepository calificacionRepository) {
        this.calificacionRepository = calificacionRepository;
    }

    @Override
    public List<Calificacion> listarTodos() {
        return calificacionRepository.findAll();
    }

    @Override
    public Optional<Calificacion> buscarPorId(Long id) {
        return calificacionRepository.findById(id);
    }

    @Override
    public List<Calificacion> buscarPorEstablecimiento(Long establecimientoId) {
        return calificacionRepository.findByEstablecimientoId(establecimientoId);
    }

    @Override
    public Calificacion guardar(Calificacion calificacion) {
        return calificacionRepository.save(calificacion);
    }

    @Override
    public void eliminar(Long id) {
        calificacionRepository.deleteById(id);
    }
}
