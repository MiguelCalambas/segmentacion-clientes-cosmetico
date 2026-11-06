package com.usc.segmentacion.negocio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.Asignacion;
import com.usc.segmentacion.negocio.entidad.AsignacionId;

public interface AsignacionRepositorio extends JpaRepository<Asignacion, AsignacionId> {
}