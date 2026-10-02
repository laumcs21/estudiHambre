package com.estudiHambre.service;

import com.estudiHambre.model.Menu;
import com.estudiHambre.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> listarTodos() {
        return menuRepository.findAll();
    }

    public Optional<Menu> buscarPorId(Long id) {
        return menuRepository.findById(id);
    }

    public List<Menu> buscarPorEstablecimiento(Long establecimientoId) {
        return menuRepository.findByEstablecimientoId(establecimientoId);
    }

    public Menu guardar(Menu menu) {
        return menuRepository.save(menu);
    }

    public void eliminar(Long id) {
        menuRepository.deleteById(id);
    }
}