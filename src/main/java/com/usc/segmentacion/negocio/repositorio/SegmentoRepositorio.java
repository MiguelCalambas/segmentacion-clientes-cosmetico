package com.usc.segmentacion.negocio.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.Segmento;

public interface SegmentoRepositorio extends JpaRepository<Segmento, Long> {
    List<Segmento> findByEjecucionIdOrderByIdAsc(Long idEjecucion);
}