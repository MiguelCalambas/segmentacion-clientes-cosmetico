package com.usc.segmentacion.negocio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.Ejecucion;

public interface EjecucionRepositorio extends JpaRepository<Ejecucion, Long> {
}