package com.usc.segmentacion.negocio.modelo;

/** Las tres variables que describen el comportamiento de compra de un cliente. */
public record VariablesCliente(String idCliente, double frecuencia, double valorTotal, double recenciaDias) {
}