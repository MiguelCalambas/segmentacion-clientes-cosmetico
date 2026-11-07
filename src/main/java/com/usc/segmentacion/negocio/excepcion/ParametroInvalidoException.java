package com.usc.segmentacion.negocio.excepcion;

/** Un parámetro de la segmentación no es válido (por ejemplo, k menor que 2). */
public class ParametroInvalidoException extends RuntimeException {
    public ParametroInvalidoException(String mensaje) {
        super(mensaje);
    }
}