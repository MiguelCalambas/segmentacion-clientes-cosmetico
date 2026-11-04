package com.usc.segmentacion.presentacion.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.usc.segmentacion.negocio.modelo.ResultadoCarga;

public record RespuestaCarga(
        @JsonProperty("id_archivo") Long idArchivo,
        String nombre,
        String estado,
        @JsonProperty("registros_leidos") int registrosLeidos,
        @JsonProperty("registros_validos") int registrosValidos,
        List<Incidencia> incidencias,
        List<String> detalles) {

    public record Incidencia(String tipo, long cantidad) {
    }

    public static RespuestaCarga desde(ResultadoCarga r) {
        List<Incidencia> lista = r.incidenciasPorTipo().entrySet().stream()
                .map(e -> new Incidencia(e.getKey(), e.getValue()))
                .toList();
        return new RespuestaCarga(r.idArchivo(), r.nombre(), r.estado(),
                r.registrosLeidos(), r.registrosValidos(), lista, r.detalles());
    }
}
