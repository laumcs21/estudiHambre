package com.estudiHambre.dto.menu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MenuActualizarDTO {

    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private List<String> imagenes = new ArrayList<>();

    public MenuActualizarDTO() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }
}
