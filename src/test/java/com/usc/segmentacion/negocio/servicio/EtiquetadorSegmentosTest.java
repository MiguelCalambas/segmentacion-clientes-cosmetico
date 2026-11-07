package com.usc.segmentacion.negocio.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.usc.segmentacion.negocio.modelo.GrupoEtiquetado;
import com.usc.segmentacion.negocio.modelo.GrupoSegmento;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EtiquetadorSegmentosTest {

    private final EtiquetadorSegmentos etiquetador = new EtiquetadorSegmentos();

    private static List<String> clientes(int cantidad) {
        return Collections.nCopies(cantidad, "c");
    }

    @Test
    void asignaLaEtiquetaSegunLosCriteriosExplicitos() {
        List<GrupoSegmento> grupos = List.of(
                new GrupoSegmento(1, clientes(60), 4.5, 1575, 47),
                new GrupoSegmento(2, clientes(120), 11, 990, 15),
                new GrupoSegmento(3, clientes(200), 3, 180, 120),
                new GrupoSegmento(4, clientes(150), 2, 80, 305));

        List<GrupoEtiquetado> resultado = etiquetador.etiquetar(grupos);

        assertEquals("Alto valor", resultado.get(0).etiqueta());
        assertEquals("Frecuentes", resultado.get(1).etiqueta());
        assertEquals("Ocasionales", resultado.get(2).etiqueta());
        assertEquals("Inactivos", resultado.get(3).etiqueta());
        assertTrue(resultado.get(0).descripcion().contains("Criterio de la etiqueta"));
    }

    @Test
    void numeraLasEtiquetasRepetidas() {
        List<GrupoSegmento> grupos = List.of(
                new GrupoSegmento(1, clientes(50), 3, 100, 50),
                new GrupoSegmento(2, clientes(50), 3, 100, 50));

        List<GrupoEtiquetado> resultado = etiquetador.etiquetar(grupos);

        assertEquals("Ocasionales", resultado.get(0).etiqueta());
        assertEquals("Ocasionales 2", resultado.get(1).etiqueta());
    }
}