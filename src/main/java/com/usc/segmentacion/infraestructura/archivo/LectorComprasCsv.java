package com.usc.segmentacion.infraestructura.archivo;

import com.usc.segmentacion.negocio.contrato.LectorCompras;
import com.usc.segmentacion.negocio.excepcion.EstructuraInvalidaException;
import com.usc.segmentacion.negocio.modelo.FilaCruda;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class LectorComprasCsv implements LectorCompras {

    private static final List<String> COLUMNAS_OBLIGATORIAS =
            List.of("id_compra", "id_cliente", "fecha_compra", "valor_compra");

    @Override
    public List<FilaCruda> leer(InputStream contenido) {
        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader lector = new InputStreamReader(contenido, StandardCharsets.UTF_8);
             CSVParser parser = formato.parse(lector)) {

            Map<String, Integer> encabezado = parser.getHeaderMap();
            if (encabezado == null || encabezado.isEmpty()) {
                throw new EstructuraInvalidaException("El archivo está vacío o no tiene encabezado");
            }

            Map<String, Integer> indices = new HashMap<>();
            encabezado.forEach((nombre, indice) ->
                    indices.put(nombre.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT), indice));

            for (String columna : COLUMNAS_OBLIGATORIAS) {
                if (!indices.containsKey(columna)) {
                    throw new EstructuraInvalidaException("Falta la columna obligatoria '" + columna + "'");
                }
            }

            List<FilaCruda> filas = new ArrayList<>();
            int numeroFila = 0;
            for (CSVRecord registro : parser) {
                numeroFila++;
                filas.add(new FilaCruda(
                        numeroFila,
                        valor(registro, indices.get("id_compra")),
                        valor(registro, indices.get("id_cliente")),
                        valor(registro, indices.get("fecha_compra")),
                        valor(registro, indices.get("valor_compra"))));
            }
            return filas;

        } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
            throw new EstructuraInvalidaException("No se pudo leer el archivo como CSV: " + e.getMessage());
        }
    }

    private static String valor(CSVRecord registro, int indice) {
        return indice < registro.size() ? registro.get(indice) : null;
    }
}