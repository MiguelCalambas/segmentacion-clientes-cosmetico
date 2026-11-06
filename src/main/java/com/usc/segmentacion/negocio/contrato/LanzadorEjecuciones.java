package com.usc.segmentacion.negocio.contrato;

public interface LanzadorEjecuciones {

    /** Procesa la ejecución sin bloquear a quien la pidió. */
    void lanzar(Long idEjecucion);
}