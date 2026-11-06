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
import java.math.BigDecimal;
import java.util.List;

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

    @Column(name = "descripcion_ia", length = 2000)
    private String descripcionIa;

    @Column(name = "ideas_campana", length = 2000)
    private String ideasCampana;

    @Column(name = "proveedor_descripcion", length = 40)
    private String proveedorDescripcion;

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

    public void registrarDescripcionIa(String descripcion, String ideas, String proveedor) {
        this.descripcionIa = recortar(descripcion, 2000);
        this.ideasCampana = recortar(ideas, 2000);
        this.proveedorDescripcion = proveedor;
    }

    /** Las ideas se guardan una por línea. */
    public List<String> ideasComoLista() {
        if (ideasCampana == null || ideasCampana.isBlank()) {
            return List.of();
        }
        return List.of(ideasCampana.split("\n"));
    }

    private static String recortar(String texto, int maximo) {
        return texto != null && texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }

    public Long getId() { return id; }
    public Ejecucion getEjecucion() { return ejecucion; }
    public String getEtiqueta() { return etiqueta; }
    public String getDescripcion() { return descripcion; }
    public int getNumClientes() { return numClientes; }
    public BigDecimal getFrecuenciaMedia() { return frecuenciaMedia; }
    public BigDecimal getValorTotalMedio() { return valorTotalMedio; }
    public BigDecimal getRecenciaMediaDias() { return recenciaMediaDias; }
    public String getDescripcionIa() { return descripcionIa; }
    public String getIdeasCampana() { return ideasCampana; }
    public String getProveedorDescripcion() { return proveedorDescripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}