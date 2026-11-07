package com.usc.segmentacion.negocio.excepcion;

/** El proveedor de IA no respondió o su respuesta no es utilizable. */
public class ProveedorIANoDisponibleException extends RuntimeException {

    public ProveedorIANoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ProveedorIANoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}