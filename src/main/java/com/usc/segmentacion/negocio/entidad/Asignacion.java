package com.usc.segmentacion.negocio.entidad;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignacion")
public class Asignacion {

    @EmbeddedId
    private AsignacionId id;

    protected Asignacion() {
    }

    public Asignacion(Long idSegmento, String idCliente) {
        this.id = new AsignacionId(idSegmento, idCliente);
    }

    public AsignacionId getId() { return id; }
}