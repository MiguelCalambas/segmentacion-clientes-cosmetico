package com.usc.segmentacion.negocio.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resumen de las compras de un cliente dentro de un archivo. */
public interface ResumenCompras {
    String getIdCliente();
    long getFrecuencia();
    BigDecimal getValorTotal();
    LocalDate getUltimaCompra();
}