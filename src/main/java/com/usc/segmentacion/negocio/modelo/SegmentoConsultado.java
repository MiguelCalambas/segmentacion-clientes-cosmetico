package com.usc.segmentacion.negocio.modelo;

import java.math.BigDecimal;

public record SegmentoConsultado(Long idSegmento, String etiqueta, int numClientes, double proporcion,
                                 BigDecimal frecuenciaMedia, BigDecimal valorTotalMedio,
                                 BigDecimal recenciaMediaDias, String descripcion) {
}