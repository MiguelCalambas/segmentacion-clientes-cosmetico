package com.usc.segmentacion.presentacion;

import com.usc.segmentacion.negocio.modelo.EstadoEjecucion;
import com.usc.segmentacion.negocio.servicio.EjecucionServicio;
import com.usc.segmentacion.presentacion.dto.RespuestaEjecucion;
import com.usc.segmentacion.presentacion.dto.RespuestaSegmentos;
import com.usc.segmentacion.presentacion.dto.SolicitudEjecucion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ejecuciones")
public class EjecucionController {

    private final EjecucionServicio servicio;

    public EjecucionController(EjecucionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<RespuestaEjecucion> crear(@Valid @RequestBody SolicitudEjecucion solicitud) {
        Integer k = solicitud.parametros() == null ? null : solicitud.parametros().k();
        EstadoEjecucion estado = servicio.crear(solicitud.idArchivo(), solicitud.tecnica(),
                solicitud.variables(), k);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(RespuestaEjecucion.resumida(estado));
    }

    @GetMapping("/{id}")
    public RespuestaEjecucion obtener(@PathVariable("id") Long id) {
        return RespuestaEjecucion.completa(servicio.obtener(id));
    }

    @GetMapping("/{id}/segmentos")
    public RespuestaSegmentos segmentos(@PathVariable("id") Long id) {
        return RespuestaSegmentos.desde(servicio.consultar(id));
    }
}