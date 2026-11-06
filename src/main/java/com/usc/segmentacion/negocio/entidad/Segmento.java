package com.usc.segmentacion.negocio.entidad;

import java.math.BigDecimal;

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
@Table(name = "segmento")
public class Segmento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_segmento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejecucion")
    private Ejecucion ejecucion;

    @Column(length = 100)
    private String etiqueta;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "num_clientes", nullable = false)
    private int numClientes;

    @Column(name = "frecuencia_media", precision = 12, scale = 2)
    private BigDecimal frecuenciaMedia;

    @Column(name = "valor_total_medio", precision = 12, scale = 2)
    private BigDecimal valorTotalMedio;

    @Column(name = "recencia_media_dias", precision = 12, scale = 2)
    private BigDecimal recenciaMediaDias;

    protected Segmento() {
    }

    public Segmento(Ejecucion ejecucion, String etiqueta, int numClientes,
                    BigDecimal frecuenciaMedia, BigDecimal valorTotalMedio, BigDecimal recenciaMediaDias) {
        this.ejecucion = ejecucion;
        this.etiqueta = etiqueta;
        this.numClientes = numClientes;
        this.frecuenciaMedia = frecuenciaMedia;
        this.valorTotalMedio = valorTotalMedio;
        this.recenciaMediaDias = recenciaMediaDias;
    }

    public Long getId() { return id; }
    public Ejecucion getEjecucion() { return ejecucion; }
    public String getEtiqueta() { return etiqueta; }
    public String getDescripcion() { return descripcion; }
    public int getNumClientes() { return numClientes; }
    public BigDecimal getFrecuenciaMedia() { return frecuenciaMedia; }
    public BigDecimal getValorTotalMedio() { return valorTotalMedio; }
    public BigDecimal getRecenciaMediaDias() { return recenciaMediaDias; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}