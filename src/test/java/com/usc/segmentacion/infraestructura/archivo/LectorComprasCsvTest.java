package com.usc.segmentacion.infraestructura.archivo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.usc.segmentacion.negocio.excepcion.EstructuraInvalidaException;
import com.usc.segmentacion.negocio.modelo.FilaCruda;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LectorComprasCsvTest {

    private final LectorComprasCsv lector = new LectorComprasCsv();

    private static InputStream csv(String texto) {
        return new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void leeLasFilasTalComoVienen() {
        String texto = """
                id_compra,id_cliente,fecha_compra,valor_compra
                1001,C-001,2026-03-15,85.50
                1002,C-002,,40.00
                """;
        List<FilaCruda> filas = lector.leer(csv(texto));
        assertEquals(2, filas.size());
        assertEquals("C-001", filas.get(0).idCliente());
        assertEquals("", filas.get(1).fecha());
    }

    @Test
    void rechazaUnArchivoSinUnaColumnaObligatoria() {
        String texto = """
                id_compra,id_cliente,valor_compra
                1001,C-001,85.50
                """;
        EstructuraInvalidaException error =
                assertThrows(EstructuraInvalidaException.class, () -> lector.leer(csv(texto)));
        assertEquals("Falta la columna obligatoria 'fecha_compra'", error.getMessage());
    }
}