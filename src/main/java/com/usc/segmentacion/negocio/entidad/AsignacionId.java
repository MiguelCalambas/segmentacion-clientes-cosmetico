package com.usc.segmentacion.negocio.entidad;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AsignacionId(
        @Column(name = "id_segmento") Long idSegmento,
        @Column(name = "id_cliente", length = 50) String idCliente) implements Serializable {
}