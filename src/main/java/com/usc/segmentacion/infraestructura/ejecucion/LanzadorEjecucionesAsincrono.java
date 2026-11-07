package com.usc.segmentacion.infraestructura.ejecucion;

import com.usc.segmentacion.negocio.contrato.LanzadorEjecuciones;
import com.usc.segmentacion.negocio.servicio.ProcesadorEjecucion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LanzadorEjecucionesAsincrono implements LanzadorEjecuciones {

    private static final Logger registro = LoggerFactory.getLogger(LanzadorEjecucionesAsincrono.class);

    private final ProcesadorEjecucion procesador;

    public LanzadorEjecucionesAsincrono(ProcesadorEjecucion procesador) {
        this.procesador = procesador;
    }

    @Async
    @Override
    public void lanzar(Long idEjecucion) {
        try {
            procesador.procesar(idEjecucion);
        } catch (RuntimeException e) {
            registro.error("La ejecución {} falló", idEjecucion, e);
            procesador.marcarFallida(idEjecucion, e.getMessage());
        }
    }
}