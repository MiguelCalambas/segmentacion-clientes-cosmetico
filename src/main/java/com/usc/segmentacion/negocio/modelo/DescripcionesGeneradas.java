package com.usc.segmentacion.negocio.modelo;

import java.util.List;

/** Lo que devolvió el asistente, indicando quién lo generó y si se usó el respaldo. */
public record DescripcionesGeneradas(String proveedor, boolean respaldo, List<DescripcionSegmento> descripciones) {
}