package com.usc.segmentacion.negocio.modelo;

/** Un grupo con su etiqueta y la descripción que explica por qué se llama así. */
public record GrupoEtiquetado(GrupoSegmento grupo, String etiqueta, String descripcion) {
}