package com.usc.segmentacion.infraestructura.segmentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.usc.segmentacion.negocio.excepcion.DatosInsuficientesException;
import com.usc.segmentacion.negocio.excepcion.ParametroInvalidoException;
import com.usc.segmentacion.negocio.modelo.GrupoSegmento;
import com.usc.segmentacion.negocio.modelo.ResultadoSegmentacion;
import com.usc.segmentacion.negocio.modelo.VariablesCliente;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class KMeansSegmentadorTest {

    private final KMeansSegmentador segmentador = new KMeansSegmentador();

    private static List<VariablesCliente> grupo(String prefijo, int cantidad, double frecuencia,
                                                double valor, double recencia, Random azar) {
        List<VariablesCliente> lista = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            lista.add(new VariablesCliente(prefijo + "-" + i,
                    frecuencia * (0.9 + azar.nextDouble() * 0.2),
                    valor * (0.9 + azar.nextDouble() * 0.2),
                    recencia * (0.9 + azar.nextDouble() * 0.2)));
        }
        return lista;
    }

    @Test
    void separaTresGruposClaramenteDistintos() {
        Random azar = new Random(1);
        List<VariablesCliente> clientes = new ArrayList<>();
        clientes.addAll(grupo("A", 30, 10, 1000, 10, azar));
        clientes.addAll(grupo("B", 30, 2, 100, 200, azar));
        clientes.addAll(grupo("C", 30, 5, 3000, 50, azar));

        ResultadoSegmentacion resultado = segmentador.segmentar(clientes, 3);

        assertEquals(3, resultado.grupos().size());
        Map<String, Set<Integer>> gruposPorPrefijo = new HashMap<>();
        for (GrupoSegmento g : resultado.grupos()) {
            assertEquals(30, g.idsClientes().size());
            for (String id : g.idsClientes()) {
                gruposPorPrefijo.computeIfAbsent(id.substring(0, 1), p -> new HashSet<>()).add(g.indice());
            }
        }
        // los clientes de un mismo origen caen todos en un solo grupo...
        assertTrue(gruposPorPrefijo.values().stream().allMatch(s -> s.size() == 1));
        // ...y los tres orígenes caen en grupos distintos
        Set<Integer> distintos = gruposPorPrefijo.values().stream()
                .map(s -> s.iterator().next())
                .collect(Collectors.toSet());
        assertEquals(3, distintos.size());
    }

    @Test
    void ordenaLosGruposDeMayorAMenorValor() {
        Random azar = new Random(2);
        List<VariablesCliente> clientes = new ArrayList<>();
        clientes.addAll(grupo("A", 20, 10, 1000, 10, azar));
        clientes.addAll(grupo("B", 20, 2, 100, 200, azar));

        ResultadoSegmentacion resultado = segmentador.segmentar(clientes, 2);

        assertEquals(1, resultado.grupos().get(0).indice());
        assertTrue(resultado.grupos().get(0).valorTotalMedio() > resultado.grupos().get(1).valorTotalMedio());
    }

    @Test
    void rechazaMenosClientesQueGrupos() {
        List<VariablesCliente> pocos = List.of(
                new VariablesCliente("A", 1, 10, 5),
                new VariablesCliente("B", 2, 20, 6));
        assertThrows(DatosInsuficientesException.class, () -> segmentador.segmentar(pocos, 3));
    }

    @Test
    void rechazaUnNumeroDeGruposInvalido() {
        List<VariablesCliente> clientes = List.of(
                new VariablesCliente("A", 1, 10, 5),
                new VariablesCliente("B", 2, 20, 6));
        assertThrows(ParametroInvalidoException.class, () -> segmentador.segmentar(clientes, 1));
    }
}