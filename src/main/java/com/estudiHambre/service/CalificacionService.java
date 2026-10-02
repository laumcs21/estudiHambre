package com.estudiHambre.service;

import com.estudiHambre.model.Calificacion;
import com.estudiHambre.repository.CalificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;

    public CalificacionService(CalificacionRepository calificacionRepository) {
        this.calificacionRepository = calificacionRepository;
    }

    public List<Calificacion> listarTodos() {
        return calificacionRepository.findAll();
    }

    public Optional<Calificacion> buscarPorId(Long id) {
        return calificacionRepository.findById(id);
    }

    public List<Calificacion> buscarPorEstablecimiento(Long establecimientoId) {
        return calificacionRepository.findByEstablecimientoId(establecimientoId);
    }

    public Calificacion guardar(Calificacion calificacion) {
        return calificacionRepository.save(calificacion);
    }

    public void eliminar(Long id) {
        calificacionRepository.deleteById(id);
    }
}