package com.usc.segmentacion.infraestructura.ia;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import com.usc.segmentacion.negocio.excepcion.ProveedorIANoDisponibleException;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

class ClienteIAGeminiTest {

    private static final String RESPUESTA_OK = """
            {"candidates":[{"content":{"parts":[{"text":"{\\"segmentos\\":[{\\"id_segmento\\":1,\\"descripcion\\":\\"Texto de prueba\\",\\"ideas_campana\\":[\\"Idea uno\\",\\"Idea dos\\"]}]}"}]},"finishReason":"STOP"}]}
            """;

    private static final List<ResumenSegmento> RESUMENES =
            List.of(new ResumenSegmento(1L, "Alto valor", 60, 0.11, 4.7, 1621.5, 50));

    private record Entrega(int codigo, String cuerpo) {
    }

    private final List<Entrega> guion = new CopyOnWriteArrayList<>();
    private final AtomicInteger llamadas = new AtomicInteger();
    private final AtomicReference<String> claveRecibida = new AtomicReference<>();
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    private HttpServer servidor;

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/v1beta/models/modelo-falso:generateContent", intercambio -> {
            int numero = llamadas.getAndIncrement();
            claveRecibida.set(intercambio.getRequestHeaders().getFirst("x-goog-api-key"));
            cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            Entrega entrega = guion.get(Math.min(numero, guion.size() - 1));
            byte[] bytes = entrega.cuerpo().getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(entrega.codigo(), bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    private ClienteIAGemini cliente() {
        String url = "http://localhost:" + servidor.getAddress().getPort() + "/v1beta";
        return new ClienteIAGemini(url, "clave-de-prueba", "modelo-falso",
                Duration.ofSeconds(5), Duration.ofMillis(10));
    }

    @Test
    void devuelveLasDescripcionesYEnviaLaClaveEnLaCabecera() {
        guion.add(new Entrega(200, RESPUESTA_OK));

        List<DescripcionSegmento> resultado = cliente().describir(RESUMENES);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).idSegmento());
        assertEquals("Texto de prueba", resultado.get(0).descripcion());
        assertEquals(2, resultado.get(0).ideasCampana().size());
        assertEquals("clave-de-prueba", claveRecibida.get());
        assertTrue(cuerpoRecibido.get().contains("Alto valor"));
        assertEquals(1, llamadas.get());
    }

    @Test
    void reintentaCuandoHayLimiteDeUsoYLuegoResponde() {
        guion.add(new Entrega(429, "{}"));
        guion.add(new Entrega(200, RESPUESTA_OK));

        List<DescripcionSegmento> resultado = cliente().describir(RESUMENES);

        assertEquals(1, resultado.size());
        assertEquals(2, llamadas.get());
    }

    @Test
    void fallaDespuesDeTresIntentosSiElServicioSiempreFalla() {
        guion.add(new Entrega(500, "{}"));

        assertThrows(ProveedorIANoDisponibleException.class, () -> cliente().describir(RESUMENES));
        assertEquals(3, llamadas.get());
    }

    @Test
    void noReintentaConUnaClaveInvalida() {
        guion.add(new Entrega(400, "{}"));

        assertThrows(ProveedorIANoDisponibleException.class, () -> cliente().describir(RESUMENES));
        assertEquals(1, llamadas.get());
    }

    @Test
    void fallaSiLaRespuestaNoEsUnJsonValido() {
        guion.add(new Entrega(200,
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"esto no es json\"}]}}]}"));

        assertThrows(ProveedorIANoDisponibleException.class, () -> cliente().describir(RESUMENES));
    }
}