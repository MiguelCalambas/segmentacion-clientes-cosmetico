package com.usc.segmentacion.negocio.repositorio;

import com.usc.segmentacion.negocio.entidad.Compra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepositorio extends JpaRepository<Compra, Long> {

    long countByArchivoId(Long idArchivo);

    @Query("""
            select c.cliente.id as idCliente,
                   count(c) as frecuencia,
                   sum(c.valorCompra) as valorTotal,
                   max(c.fechaCompra) as ultimaCompra
            from Compra c
            where c.archivo.id = :idArchivo
            group by c.cliente.id
            """)
    List<ResumenCompras> resumenPorCliente(@Param("idArchivo") Long idArchivo);

    @Query("select count(distinct c.cliente.id) from Compra c where c.archivo.id = :idArchivo")
    long contarClientes(@Param("idArchivo") Long idArchivo);
}