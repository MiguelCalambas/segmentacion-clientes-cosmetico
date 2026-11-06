package com.usc.segmentacion.negocio.contrato;

import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;
import java.util.List;

public interface AsistenteDescriptivo {

    String NOMBRE_PLANTILLA = "plantilla";

    /** Nombre del proveedor, por ejemplo "plantilla" o "gemini". */
    String nombre();

    /**
     * Redacta una descripción y unas ideas de campaña para cada segmento, a partir de sus resúmenes.
     * Lanza ProveedorIANoDisponibleException si no puede responder.
     */
    List<DescripcionSegmento> describir(List<ResumenSegmento> resumenes);
}