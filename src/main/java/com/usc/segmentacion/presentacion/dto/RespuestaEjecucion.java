package com.usc.segmentacion.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usc.segmentacion.negocio.modelo.EstadoEjecucion;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaEjecucion(
        @JsonProperty("id_ejecucion") Long idEjecucion,
        @JsonProperty("id_archivo") Long idArchivo,
        String estado,
        String tecnica,
        List<String> variables,
        SolicitudEjecucion.Parametros parametros,
        @JsonProperty("mensaje_error") String mensajeError) {

    /** Respuesta corta al crear: coincide con la especificación de la API. */
    public static RespuestaEjecucion resumida(EstadoEjecucion e) {
        return new RespuestaEjecucion(e.idEjecucion(), e.idArchivo(), e.estado(), null, null, null, null);
    }

    public static RespuestaEjecucion completa(EstadoEjecucion e) {
        return new RespuestaEjecucion(e.idEjecucion(), e.idArchivo(), e.estado(), e.tecnica(),
                List.of(e.variables().split(",")), leerParametros(e.parametros()), e.mensajeError());
    }

    private static SolicitudEjecucion.Parametros leerParametros(String texto) {
        if (texto != null && texto.startsWith("k=")) {
            return new SolicitudEjecucion.Parametros(Integer.parseInt(texto.substring(2).trim()));
        }
        return null;
    }
}