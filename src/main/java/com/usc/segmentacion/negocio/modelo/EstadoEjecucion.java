package com.usc.segmentacion.negocio.modelo;

public record EstadoEjecucion(Long idEjecucion, Long idArchivo, String tecnica, String variables,
 String parametros, String estado, String mensajeError) {
}