package com.estudiHambre.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "beneficios_estudiantiles")
public class BeneficioEstudiantil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBeneficioEstudiantil tipo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "porcentaje_descuento")
    private Integer porcentajeDescuento;

    @Column(name = "cantidad_comidas")
    private Integer cantidadComidas;

    @Column(name = "precio_tiquetera", precision = 12, scale = 2)
    private BigDecimal precioTiquetera;

    @Column(length = 80)
    private String codigo;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    public BeneficioEstudiantil() {
    }

    public Long getId() {
        return id;
    }

    public TipoBeneficioEstudiantil getTipo() {
        return tipo;
    }

    public void setTipo(TipoBeneficioEstudiantil tipo) {
        this.tipo = tipo;
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

    public Integer getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Integer porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Integer getCantidadComidas() {
        return cantidadComidas;
    }

    public void setCantidadComidas(Integer cantidadComidas) {
        this.cantidadComidas = cantidadComidas;
    }

    public BigDecimal getPrecioTiquetera() {
        return precioTiquetera;
    }

    public void setPrecioTiquetera(BigDecimal precioTiquetera) {
        this.precioTiquetera = precioTiquetera;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }
}
