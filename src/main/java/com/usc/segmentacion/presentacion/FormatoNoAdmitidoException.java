package com.usc.segmentacion.presentacion;

/** El archivo enviado no es del tipo esperado (.csv) o está vacío. */
public class FormatoNoAdmitidoException extends RuntimeException {
    public FormatoNoAdmitidoException(String mensaje) {
        super(mensaje);
    }
}