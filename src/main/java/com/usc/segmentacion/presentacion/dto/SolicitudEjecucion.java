package com.usc.segmentacion.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SolicitudEjecucion(
        @NotNull(message = "id_archivo es obligatorio") @JsonProperty("id_archivo") Long idArchivo,
        String tecnica,
        List<String> variables,
        Parametros parametros) {

    public record Parametros(Integer k) {
    }
}