package com.usc.segmentacion.negocio.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @Column(name = "id_cliente", length = 50)
    private String id;

    protected Cliente() {
    }

    public Cliente(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}