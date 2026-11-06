package com.usc.segmentacion.negocio.modelo;

import java.util.List;

public record DescripcionSegmento(Long idSegmento, String descripcion, List<String> ideasCampana) {
}