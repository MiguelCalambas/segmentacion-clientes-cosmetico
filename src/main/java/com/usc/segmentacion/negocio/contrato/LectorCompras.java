package com.usc.segmentacion.negocio.contrato;

import com.usc.segmentacion.negocio.modelo.FilaCruda;
import java.io.InputStream;
import java.util.List;

public interface LectorCompras {

    /**
     * Lee el contenido y devuelve las filas tal como vienen, sin validar los valores.
     * Lanza EstructuraInvalidaException si faltan columnas obligatorias.
     */
    List<FilaCruda> leer(InputStream contenido);
}