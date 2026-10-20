package com.estudiHambre.mapper;

import com.estudiHambre.dto.menu.MenuCrearDTO;
import com.estudiHambre.dto.menu.MenuActualizarDTO;
import com.estudiHambre.dto.menu.MenuRespuestaDTO;
import com.estudiHambre.model.Menu;
import com.estudiHambre.model.MenuImagen;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

    public Menu toEntity(MenuCrearDTO dto) {

        Menu menu = new Menu();

        menu.setTitulo(dto.getTitulo().trim());
        menu.setDescripcion(dto.getDescripcion());
        menu.setPrecio(dto.getPrecio());
        menu.setDisponible(dto.isDisponible());

        return menu;
    }

    public MenuRespuestaDTO toDTO(Menu menu) {

        MenuRespuestaDTO dto = new MenuRespuestaDTO();

        dto.setId(menu.getId());
        dto.setTitulo(menu.getTitulo());
        dto.setDescripcion(menu.getDescripcion());
        dto.setPrecio(menu.getPrecio());
        dto.setDisponible(menu.isDisponible());

        dto.setEstablecimientoId(
                menu.getEstablecimiento().getId()
        );

        dto.setImagenes(
                menu.getImagenes()
                        .stream()
                        .map(MenuImagen::getUrl)
                        .toList()
        );

        return dto;
    }

    public void actualizarEntidad(MenuActualizarDTO dto, Menu menu) {

        if (dto.getTitulo() != null) {
            menu.setTitulo(dto.getTitulo().trim());
        }

        if (dto.getDescripcion() != null) {
            menu.setDescripcion(dto.getDescripcion());
        }

        if (dto.getPrecio() != null) {
            menu.setPrecio(dto.getPrecio());
        }

        if (dto.getDisponible() != null) {
            menu.setDisponible(dto.getDisponible());
        }
    }
}