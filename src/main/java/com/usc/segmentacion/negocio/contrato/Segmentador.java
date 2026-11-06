package com.usc.segmentacion.negocio.contrato;

import com.usc.segmentacion.negocio.modelo.ResultadoSegmentacion;
import com.usc.segmentacion.negocio.modelo.VariablesCliente;
import java.util.List;

public interface Segmentador {

    /**
     * Agrupa a los clientes según su comportamiento de compra.
     * Lanza ParametroInvalidoException o DatosInsuficientesException si no se puede.
     */
    ResultadoSegmentacion segmentar(List<VariablesCliente> clientes, int numeroGrupos);
}