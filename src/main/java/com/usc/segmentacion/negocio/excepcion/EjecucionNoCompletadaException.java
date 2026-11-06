package com.usc.segmentacion.negocio.excepcion;

/** Se pidieron los segmentos de una ejecución que aún no terminó o que falló. */
public class EjecucionNoCompletadaException extends RuntimeException {

    private final String estado;

    public EjecucionNoCompletadaException(String estado, String mensajeError) {
        super(mensajeError == null
                ? "La ejecución está en estado '" + estado + "'"
                : "La ejecución está en estado '" + estado + "': " + mensajeError);
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }
}