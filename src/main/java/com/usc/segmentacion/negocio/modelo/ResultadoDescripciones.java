package com.usc.segmentacion.negocio.modelo;

import java.util.List;

public record ResultadoDescripciones(Long idEjecucion, String proveedor, boolean respaldo, boolean generadoPorIa, List<SegmentoDescrito> segmentos) {
}