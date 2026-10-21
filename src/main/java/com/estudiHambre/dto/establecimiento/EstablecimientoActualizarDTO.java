package com.estudiHambre.dto.establecimiento;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EstablecimientoActualizarDTO {

    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
    private String nombre;

    @Size(min = 1, max = 50, message = "El tipo debe tener entre 1 y 50 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "El tipo no puede estar vacío")
    private String tipo;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @Size(min = 1, max = 200, message = "La dirección debe tener entre 1 y 200 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "La dirección no puede estar vacía")
    private String direccion;

    @Pattern(
            regexp = "^$|^[0-9+()\\s-]{7,20}$",
            message = "El teléfono debe tener un formato válido"
    )
    private String telefono;

    @DecimalMin(value = "-90.0", message = "La latitud no puede ser menor que -90")
    @DecimalMax(value = "90.0", message = "La latitud no puede ser mayor que 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud no puede ser menor que -180")
    @DecimalMax(value = "180.0", message = "La longitud no puede ser mayor que 180")
    private Double longitud;

    public EstablecimientoActualizarDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}