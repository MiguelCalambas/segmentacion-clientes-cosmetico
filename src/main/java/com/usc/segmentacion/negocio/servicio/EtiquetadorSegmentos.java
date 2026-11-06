package com.usc.segmentacion.negocio.servicio;

import com.usc.segmentacion.negocio.modelo.GrupoEtiquetado;
import com.usc.segmentacion.negocio.modelo.GrupoSegmento;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import org.springframework.stereotype.Component;

@Component
public class EtiquetadorSegmentos {

    /** Un grupo se considera "alto" si supera en más de 25 % el promedio general. */
    private static final double UMBRAL = 1.25;

    public List<GrupoEtiquetado> etiquetar(List<GrupoSegmento> grupos) {
        int total = grupos.stream().mapToInt(g -> g.idsClientes().size()).sum();
        double frecuenciaGeneral = promedio(grupos, GrupoSegmento::frecuenciaMedia, total);
        double valorGeneral = promedio(grupos, GrupoSegmento::valorTotalMedio, total);
        double recenciaGeneral = promedio(grupos, GrupoSegmento::recenciaMediaDias, total);

        Map<String, Integer> usadas = new HashMap<>();
        List<GrupoEtiquetado> resultado = new ArrayList<>();
        for (GrupoSegmento g : grupos) {
            String base;
            if (g.recenciaMediaDias() > recenciaGeneral * UMBRAL) {
                base = "Inactivos";
            } else if (g.frecuenciaMedia() > frecuenciaGeneral * UMBRAL) {
                base = "Frecuentes";
            } else if (g.valorTotalMedio() > valorGeneral * UMBRAL) {
                base = "Alto valor";
            } else {
                base = "Ocasionales";
            }
            int veces = usadas.merge(base, 1, Integer::sum);
            String etiqueta = veces == 1 ? base : base + " " + veces;
            resultado.add(new GrupoEtiquetado(g, etiqueta,
                    descripcion(base, g, frecuenciaGeneral, valorGeneral, recenciaGeneral)));
        }
        return resultado;
    }

    private static double promedio(List<GrupoSegmento> grupos, ToDoubleFunction<GrupoSegmento> variable, int total) {
        double suma = 0;
        for (GrupoSegmento g : grupos) {
            suma += variable.applyAsDouble(g) * g.idsClientes().size();
        }
        return suma / total;
    }

    private static String descripcion(String base, GrupoSegmento g, double frecuenciaGeneral,
                                      double valorGeneral, double recenciaGeneral) {
        String criterio = switch (base) {
            case "Inactivos" -> "su última compra es más de 25 % más antigua que el promedio general";
            case "Frecuentes" -> "compran con una frecuencia más de 25 % mayor que el promedio general";
            case "Alto valor" -> "su gasto total es más de 25 % mayor que el promedio general";
            default -> "no superan en 25 % el promedio general en frecuencia ni en gasto, "
                    + "y su última compra no es especialmente antigua";
        };
        return String.format(Locale.ROOT,
                "Compran %.1f veces en promedio (general %.1f), gastan %.2f en total (general %.2f) "
                        + "y su última compra fue hace %.0f días (general %.0f). Criterio de la etiqueta: %s.",
                g.frecuenciaMedia(), frecuenciaGeneral, g.valorTotalMedio(), valorGeneral,
                g.recenciaMediaDias(), recenciaGeneral, criterio);
    }
}