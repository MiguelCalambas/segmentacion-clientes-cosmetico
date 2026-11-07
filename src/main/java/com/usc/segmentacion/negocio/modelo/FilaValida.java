package com.usc.segmentacion.negocio.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una fila que ya pasó todas las validaciones. */
public record FilaValida(String idCompra, String idCliente, LocalDate fecha, BigDecimal valor) {
}