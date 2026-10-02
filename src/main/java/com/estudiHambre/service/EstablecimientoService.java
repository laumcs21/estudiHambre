package com.estudiHambre.service;

import com.estudiHambre.model.Establecimiento;
import com.estudiHambre.repository.EstablecimientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstablecimientoService {

    private final EstablecimientoRepository establecimientoRepository;

    public EstablecimientoService(EstablecimientoRepository establecimientoRepository) {
        this.establecimientoRepository = establecimientoRepository;
    }

    public List<Establecimiento> listarTodos() {
        return establecimientoRepository.findAll();
    }

    public Optional<Establecimiento> buscarPorId(Long id) {
        return establecimientoRepository.findById(id);
    }

    public Establecimiento guardar(Establecimiento establecimiento) {
        return establecimientoRepository.save(establecimiento);
    }

    public void eliminar(Long id) {
        establecimientoRepository.deleteById(id);
    }
}