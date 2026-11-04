package com.usc.segmentacion.negocio.modelo;

/** Una fila del archivo tal como viene, sin validar. */
public record FilaCruda(int fila, String idCompra, String idCliente, String fecha, String valor) {
}