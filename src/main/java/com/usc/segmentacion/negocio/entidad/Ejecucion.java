package com.usc.segmentacion.negocio.entidad;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ejecucion")
public class Ejecucion {

    public static final String EN_PROCESO = "en_proceso";
    public static final String COMPLETADA = "completada";
    public static final String FALLIDA = "fallida";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejecucion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_archivo")
    private ArchivoCarga archivo;

    @Column(nullable = false, length = 40)
    private String tecnica;

    @Column(name = "variables_usadas", nullable = false)
    private String variablesUsadas;

    private String parametros;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    protected Ejecucion() {
    }

    public Ejecucion(ArchivoCarga archivo, String tecnica, String variablesUsadas, String parametros) {
        this.archivo = archivo;
        this.tecnica = tecnica;
        this.variablesUsadas = variablesUsadas;
        this.parametros = parametros;
        this.estado = EN_PROCESO;
    }

    @PrePersist
    void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public void completar() {
        this.estado = COMPLETADA;
    }

    public void fallar(String mensaje) {
        this.estado = FALLIDA;
        this.mensajeError = mensaje != null && mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
    }

    public Long getId() { return id; }
    public ArchivoCarga getArchivo() { return archivo; }
    public String getTecnica() { return tecnica; }
    public String getVariablesUsadas() { return variablesUsadas; }
    public String getParametros() { return parametros; }
    public LocalDateTime getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getMensajeError() { return mensajeError; }
}