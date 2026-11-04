package com.usc.segmentacion.negocio.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.IncidenciaCalidad;

public interface IncidenciaCalidadRepositorio extends JpaRepository<IncidenciaCalidad, Long> {
    List<IncidenciaCalidad> findByArchivoId(Long idArchivo);
}