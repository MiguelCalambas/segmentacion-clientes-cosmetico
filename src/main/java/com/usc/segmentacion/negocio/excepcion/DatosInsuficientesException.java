package com.usc.segmentacion.negocio.excepcion;

/** No hay suficientes clientes para formar los grupos pedidos. */
public class DatosInsuficientesException extends RuntimeException {
    public DatosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}