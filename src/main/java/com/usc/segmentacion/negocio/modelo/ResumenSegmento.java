package com.usc.segmentacion.negocio.modelo;

/** Lo único que se envía al proveedor: datos agregados, sin información de clientes. */
public record ResumenSegmento(Long idSegmento, String etiqueta, int numClientes, double proporcion,
double frecuenciaMedia, double valorTotalMedio, double recenciaMediaDias) {
}