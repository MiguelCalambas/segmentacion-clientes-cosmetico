package com.usc.segmentacion.negocio.modelo;

import java.util.List;

public record SegmentoDescrito(Long idSegmento, String etiqueta, String descripcion, List<String> ideasCampana) {
}