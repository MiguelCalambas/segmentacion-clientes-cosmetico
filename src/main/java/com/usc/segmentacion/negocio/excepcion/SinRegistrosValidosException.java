package com.usc.segmentacion.negocio.excepcion;

/** El archivo se leyó bien, pero ninguna fila pasó las validaciones. */
public class SinRegistrosValidosException extends RuntimeException {
    public SinRegistrosValidosException(String mensaje) {
        super(mensaje);
    }
}