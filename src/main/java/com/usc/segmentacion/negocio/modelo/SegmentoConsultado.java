package com.usc.segmentacion.negocio.modelo;

import java.math.BigDecimal;
import java.util.List;

public record SegmentoConsultado(Long idSegmento, String etiqueta, int numClientes, double proporcion,
BigDecimal frecuenciaMedia, BigDecimal valorTotalMedio, BigDecimal recenciaMediaDias, String descripcion,
String descripcionIa, List<String> ideasCampana, String proveedorDescripcion) {
}