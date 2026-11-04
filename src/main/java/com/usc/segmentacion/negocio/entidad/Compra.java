package com.usc.segmentacion.negocio.entidad;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_archivo")
    private ArchivoCarga archivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @Column(name = "codigo_compra", nullable = false, length = 50)
    private String codigoCompra;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "valor_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorCompra;

    protected Compra() {
    }

    public Compra(ArchivoCarga archivo, Cliente cliente, String codigoCompra,
                  LocalDate fechaCompra, BigDecimal valorCompra) {
        this.archivo = archivo;
        this.cliente = cliente;
        this.codigoCompra = codigoCompra;
        this.fechaCompra = fechaCompra;
        this.valorCompra = valorCompra;
    }

    public Long getId() { return id; }
    public ArchivoCarga getArchivo() { return archivo; }
    public Cliente getCliente() { return cliente; }
    public String getCodigoCompra() { return codigoCompra; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public BigDecimal getValorCompra() { return valorCompra; }
}