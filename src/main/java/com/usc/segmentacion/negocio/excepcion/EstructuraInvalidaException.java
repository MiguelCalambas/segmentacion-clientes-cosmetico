package com.usc.segmentacion.negocio.excepcion;

/** El archivo no tiene la estructura esperada (por ejemplo, falta una columna). */
public class EstructuraInvalidaException extends RuntimeException {
    public EstructuraInvalidaException(String mensaje) {
        super(mensaje);
    }
}