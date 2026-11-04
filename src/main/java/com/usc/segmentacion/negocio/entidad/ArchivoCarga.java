package com.usc.segmentacion.negocio.entidad;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "archivo_carga")
public class ArchivoCarga {

    public static final String VALIDADO = "validado";
    public static final String VALIDADO_CON_OBSERVACIONES = "validado_con_observaciones";
    public static final String RECHAZADO = "rechazado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Column(nullable = false)
    private String estado;

    @Column(name = "registros_leidos", nullable = false)
    private int registrosLeidos;

    protected ArchivoCarga() {
    }

    public ArchivoCarga(String nombre) {
        this.nombre = nombre;
        this.estado = RECHAZADO;
    }

    @PrePersist
    void antesDeGuardar() {
        if (fechaCarga == null) {
            fechaCarga = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public String getEstado() { return estado; }
    public int getRegistrosLeidos() { return registrosLeidos; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setRegistrosLeidos(int registrosLeidos) { this.registrosLeidos = registrosLeidos; }
}