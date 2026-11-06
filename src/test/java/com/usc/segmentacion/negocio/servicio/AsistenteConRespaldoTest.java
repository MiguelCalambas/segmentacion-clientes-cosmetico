package com.usc.segmentacion.negocio.servicio;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;
import com.usc.segmentacion.negocio.excepcion.ProveedorIANoDisponibleException;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.DescripcionesGeneradas;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

class AsistenteConRespaldoTest {

    private static final List<ResumenSegmento> RESUMENES =
            List.of(new ResumenSegmento(1L, "Alto valor", 60, 0.11, 4.7, 1621.5, 50));

    private static AsistenteDescriptivo falso(String nombre, Supplier<List<DescripcionSegmento>> respuesta) {
        return new AsistenteDescriptivo() {
            @Override
            public String nombre() {
                return nombre;
            }

            @Override
            public List<DescripcionSegmento> describir(List<ResumenSegmento> resumenes) {
                return respuesta.get();
            }
        };
    }

    private static final AsistenteDescriptivo PLANTILLA = falso("plantilla",
            () -> List.of(new DescripcionSegmento(1L, "texto de plantilla", List.of("idea"))));

    @Test
    void usaElProveedorPrincipalCuandoResponde() {
        AsistenteDescriptivo principal = falso("gemini",
                () -> List.of(new DescripcionSegmento(1L, "texto de la IA", List.of("idea de la IA"))));

        DescripcionesGeneradas r = new AsistenteConRespaldo(principal, PLANTILLA).describir(RESUMENES);

        assertEquals("gemini", r.proveedor());
        assertFalse(r.respaldo());
        assertEquals("texto de la IA", r.descripciones().get(0).descripcion());
    }

    @Test
    void usaLaPlantillaCuandoElProveedorFalla() {
        AsistenteDescriptivo principal = falso("gemini", () -> {
            throw new ProveedorIANoDisponibleException("sin conexión");
        });

        DescripcionesGeneradas r = new AsistenteConRespaldo(principal, PLANTILLA).describir(RESUMENES);

        assertEquals("plantilla", r.proveedor());
        assertTrue(r.respaldo());
        assertEquals("texto de plantilla", r.descripciones().get(0).descripcion());
    }

    @Test
    void usaLaPlantillaCuandoLaRespuestaEstaIncompleta() {
        AsistenteDescriptivo principal = falso("gemini", () -> List.of());

        DescripcionesGeneradas r = new AsistenteConRespaldo(principal, PLANTILLA).describir(RESUMENES);

        assertEquals("plantilla", r.proveedor());
        assertTrue(r.respaldo());
    }
}