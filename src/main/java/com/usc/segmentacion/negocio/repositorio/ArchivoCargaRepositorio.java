package com.usc.segmentacion.negocio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.ArchivoCarga;

public interface ArchivoCargaRepositorio extends JpaRepository<ArchivoCarga, Long> {
}