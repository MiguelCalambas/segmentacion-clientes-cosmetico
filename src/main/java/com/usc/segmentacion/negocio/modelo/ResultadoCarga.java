package com.usc.segmentacion.negocio.modelo;

import java.util.List;
import java.util.Map;

public record ResultadoCarga(
        Long idArchivo,
        String nombre,
        String estado,
        int registrosLeidos,
        int registrosValidos,
        Map<String, Long> incidenciasPorTipo,
        List<String> detalles) {
}