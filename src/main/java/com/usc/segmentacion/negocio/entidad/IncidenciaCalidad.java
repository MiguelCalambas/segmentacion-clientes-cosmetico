package com.usc.segmentacion.negocio.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidencia_calidad")
public class IncidenciaCalidad {

    public static final String CAMPO_VACIO = "campo_vacio";
    public static final String FORMATO_INVALIDO = "formato_invalido";
    public static final String DUPLICADO = "duplicado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_archivo")
    private ArchivoCarga archivo;

    @Column(name = "tipo_incidencia", nullable = false)
    private String tipo;

    @Column(length = 500)
    private String detalle;

    protected IncidenciaCalidad() {
    }

    public IncidenciaCalidad(ArchivoCarga archivo, String tipo, String detalle) {
        this.archivo = archivo;
        this.tipo = tipo;
        this.detalle = detalle;
    }

    public Long getId() { return id; }
    public ArchivoCarga getArchivo() { return archivo; }
    public String getTipo() { return tipo; }
    public String getDetalle() { return detalle; }
}