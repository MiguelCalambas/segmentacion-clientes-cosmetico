package com.usc.segmentacion.infraestructura.ia;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

class AsistentePlantillaTest {

    private final AsistentePlantilla plantilla = new AsistentePlantilla();

    @Test
    void generaDescripcionEIdeasPorEtiqueta() {
        List<DescripcionSegmento> resultado = plantilla.describir(List.of(
                new ResumenSegmento(1L, "Inactivos", 150, 0.28, 1.95, 79.49, 302.87),
                new ResumenSegmento(2L, "Ocasionales 2", 50, 0.10, 3.0, 150.0, 100.0)));

        assertEquals("plantilla", plantilla.nombre());
        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).descripcion().contains("150 clientes"));
        assertEquals(3, resultado.get(0).ideasCampana().size());
        // una etiqueta con número usa las ideas de su etiqueta base
        assertEquals(3, resultado.get(1).ideasCampana().size());
    }
}