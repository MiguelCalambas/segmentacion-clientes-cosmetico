package com.usc.segmentacion.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usc.segmentacion.negocio.modelo.ConsultaSegmentos;
import java.math.BigDecimal;
import java.util.List;

public record RespuestaSegmentos(
        @JsonProperty("id_ejecucion") Long idEjecucion,
        String estado,
        List<SegmentoDto> segmentos) {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record SegmentoDto(
            @JsonProperty("id_segmento") Long idSegmento,
            String etiqueta,
            @JsonProperty("num_clientes") int numClientes,
            double proporcion,
            Estadisticas estadisticas,
            String descripcion,
            @JsonProperty("descripcion_ia") String descripcionIa,
            @JsonProperty("ideas_campana") List<String> ideasCampana,
            @JsonProperty("proveedor_ia") String proveedorIa) {
    }

    public record Estadisticas(
            @JsonProperty("frecuencia_media") BigDecimal frecuenciaMedia,
            @JsonProperty("valor_total_medio") BigDecimal valorTotalMedio,
            @JsonProperty("recencia_media_dias") BigDecimal recenciaMediaDias) {
    }

    public static RespuestaSegmentos desde(ConsultaSegmentos c) {
        List<SegmentoDto> lista = c.segmentos().stream()
                .map(s -> new SegmentoDto(s.idSegmento(), s.etiqueta(), s.numClientes(), s.proporcion(),
                        new Estadisticas(s.frecuenciaMedia(), s.valorTotalMedio(), s.recenciaMediaDias()),
                        s.descripcion(), s.descripcionIa(), s.ideasCampana(), s.proveedorDescripcion()))
                .toList();
        return new RespuestaSegmentos(c.idEjecucion(), c.estado(), lista);
    }
}