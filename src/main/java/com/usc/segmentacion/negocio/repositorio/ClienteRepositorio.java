package com.usc.segmentacion.negocio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usc.segmentacion.negocio.entidad.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, String> {
}