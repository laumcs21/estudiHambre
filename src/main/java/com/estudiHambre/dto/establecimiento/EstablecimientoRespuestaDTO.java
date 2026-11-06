package com.estudiHambre.dto.establecimiento;

import com.estudiHambre.model.EstadoEstablecimiento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public class EstablecimientoRespuestaDTO {

    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String imagenUrl;
    private Double latitud;
    private Double longitud;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaApertura;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaCierre;

    private Boolean cerradoTemporalmente;
    private Boolean abiertoAhora;
    private EstadoEstablecimiento estado;

    public EstablecimientoRespuestaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
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

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public Boolean getCerradoTemporalmente() {
        return cerradoTemporalmente;
    }

    public void setCerradoTemporalmente(Boolean cerradoTemporalmente) {
        this.cerradoTemporalmente = cerradoTemporalmente;
    }

    public Boolean getAbiertoAhora() {
        return abiertoAhora;
    }

    public void setAbiertoAhora(Boolean abiertoAhora) {
        this.abiertoAhora = abiertoAhora;
    }

    public EstadoEstablecimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEstablecimiento estado) {
        this.estado = estado;
    }
}
