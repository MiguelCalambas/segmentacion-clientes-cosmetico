package com.usc.segmentacion.negocio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.Compra;

public interface CompraRepositorio extends JpaRepository<Compra, Long> {
    long countByArchivoId(Long idArchivo);
}