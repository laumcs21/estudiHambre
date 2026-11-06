
package com.estudiHambre.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

@Entity
@Table(name = "establecimientos")
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String tipo;

    private String descripcion;

    private String direccion;

    private String telefono;

    private String imagenUrl;

    @Column(name = "imagen_public_id")
    private String imagenPublicId;

    private Double latitud;

    private Double longitud;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    @Column(name = "cerrado_temporalmente")
    private Boolean cerradoTemporalmente = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Usuario propietario;

    @OneToMany(
            mappedBy = "establecimiento",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "establecimiento")
    private List<Calificacion> calificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "establecimiento")
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(
            mappedBy = "establecimiento",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BeneficioEstudiantil> beneficiosEstudiantiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEstablecimiento estado = EstadoEstablecimiento.PENDIENTE;

    public Establecimiento() {
    }

    public Establecimiento(
            String nombre,
            String tipo,
            String descripcion,
            String direccion,
            String telefono,
            String imagenUrl,
            Double latitud,
            Double longitud
    ) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.imagenUrl = imagenUrl;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() {
        return id;
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

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public List<Calificacion> getCalificaciones() {
        return calificaciones;
    }

    public void setCalificaciones(List<Calificacion> calificaciones) {
        this.calificaciones = calificaciones;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public void setPropietario(Usuario propietario) {
        this.propietario = propietario;
    }

    public EstadoEstablecimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEstablecimiento estado) {
        this.estado = estado;
    }

    public String getImagenPublicId() {
        return imagenPublicId;
    }

    public void setImagenPublicId(String imagenPublicId) {
        this.imagenPublicId = imagenPublicId;
    }
}
