package com.usc.segmentacion.presentacion.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.usc.segmentacion.negocio.modelo.ResultadoDescripciones;

public record RespuestaDescripciones(
        @JsonProperty("id_ejecucion") Long idEjecucion,
        String proveedor,
        boolean respaldo,
        @JsonProperty("generado_por_ia") boolean generadoPorIa,
        List<SegmentoDescritoDto> segmentos) {

    public record SegmentoDescritoDto(
            @JsonProperty("id_segmento") Long idSegmento,
            String etiqueta,
            String descripcion,
            @JsonProperty("ideas_campana") List<String> ideasCampana) {
    }

    public static RespuestaDescripciones desde(ResultadoDescripciones r) {
        List<SegmentoDescritoDto> lista = r.segmentos().stream()
                .map(s -> new SegmentoDescritoDto(s.idSegmento(), s.etiqueta(), s.descripcion(), s.ideasCampana()))
                .toList();
        return new RespuestaDescripciones(r.idEjecucion(), r.proveedor(), r.respaldo(), r.generadoPorIa(), lista);
    }
}