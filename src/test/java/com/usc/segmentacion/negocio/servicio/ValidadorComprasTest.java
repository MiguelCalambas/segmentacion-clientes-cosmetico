package com.usc.segmentacion.negocio.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.usc.segmentacion.negocio.entidad.IncidenciaCalidad;
import com.usc.segmentacion.negocio.modelo.FilaCruda;
import com.usc.segmentacion.negocio.modelo.ResultadoValidacion;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorComprasTest {

    private final ValidadorCompras validador = new ValidadorCompras();

    private static FilaCruda fila(int numero, String compra, String cliente, String fecha, String valor) {
        return new FilaCruda(numero, compra, cliente, fecha, valor);
    }

    @Test
    void aceptaFilasCorrectas() {
        ResultadoValidacion r = validador.validar(List.of(fila(1, "1001", "C-001", "2026-03-15", "85.50")));
        assertEquals(1, r.validas().size());
        assertTrue(r.incidencias().isEmpty());
    }

    @Test
    void detectaCamposVacios() {
        ResultadoValidacion r = validador.validar(List.of(fila(1, "1001", "C-001", "", "85.50")));
        assertTrue(r.validas().isEmpty());
        assertEquals(IncidenciaCalidad.CAMPO_VACIO, r.incidencias().get(0).tipo());
    }

    @Test
    void detectaFechaConFormatoInvalido() {
        ResultadoValidacion r = validador.validar(List.of(fila(1, "1001", "C-001", "15/03/2026", "85.50")));
        assertEquals(IncidenciaCalidad.FORMATO_INVALIDO, r.incidencias().get(0).tipo());
    }

    @Test
    void detectaValorNoPositivo() {
        ResultadoValidacion r = validador.validar(List.of(fila(1, "1001", "C-001", "2026-03-15", "-5")));
        assertEquals(IncidenciaCalidad.FORMATO_INVALIDO, r.incidencias().get(0).tipo());
    }

    @Test
    void detectaCompraDuplicada() {
        ResultadoValidacion r = validador.validar(List.of(
                fila(1, "1001", "C-001", "2026-03-15", "85.50"),
                fila(2, "1001", "C-002", "2026-03-16", "40.00")));
        assertEquals(1, r.validas().size());
        assertEquals(IncidenciaCalidad.DUPLICADO, r.incidencias().get(0).tipo());
    }
}