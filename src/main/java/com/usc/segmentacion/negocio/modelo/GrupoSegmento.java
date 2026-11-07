package com.usc.segmentacion.negocio.modelo;

import java.util.List;

/** Un grupo de clientes parecidos, con el promedio de cada variable. */
public record GrupoSegmento(int indice, List<String> idsClientes,
                            double frecuenciaMedia, double valorTotalMedio, double recenciaMediaDias) {
}