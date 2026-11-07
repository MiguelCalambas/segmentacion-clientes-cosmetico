package com.usc.segmentacion.negocio.modelo;

import java.util.List;

public record ResultadoValidacion(List<FilaValida> validas, List<IncidenciaDetectada> incidencias) {
}