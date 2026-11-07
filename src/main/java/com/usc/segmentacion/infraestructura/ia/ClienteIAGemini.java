package com.usc.segmentacion.infraestructura.ia;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;
import com.usc.segmentacion.negocio.excepcion.ProveedorIANoDisponibleException;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

/**
 * Adaptador de la API de Gemini (Google). Solo recibe resúmenes agregados por segmento.
 * Reintenta ante límites de uso o fallos del servicio y convierte cualquier problema en
 * ProveedorIANoDisponibleException, para que el sistema use la plantilla de respaldo.
 */
public class ClienteIAGemini implements AsistenteDescriptivo {

    public static final String NOMBRE = "gemini";

    private static final Logger registro = LoggerFactory.getLogger(ClienteIAGemini.class);
    private static final int MAXIMO_INTENTOS = 3; // 1 intento y 2 reintentos

    private static final String INSTRUCCION = """
            Eres un analista de marketing del sector cosmético. Recibirás resúmenes agregados de segmentos \
            de clientes (promedios y conteos, nunca datos individuales). Para cada segmento redacta, en español, \
            una descripción de máximo dos frases y de dos a tres ideas de campaña, promoción o mensaje. \
            Las ideas son orientativas: no prometas resultados ni inventes cifras distintas de las recibidas. \
            Responde SOLO con JSON válido, sin texto adicional ni formato Markdown, con esta forma exacta: \
            {"segmentos":[{"id_segmento":1,"descripcion":"...","ideas_campana":["...","..."]}]}""";

    private final RestClient cliente;
    private final String modelo;
    private final Duration pausaBase;

    public ClienteIAGemini(String urlBase, String clave, String modelo, Duration tiempoLimite, Duration pausaBase) {
        HttpClient http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(tiempoLimite)
                .build();
        JdkClientHttpRequestFactory factoria = new JdkClientHttpRequestFactory(http);
        factoria.setReadTimeout(tiempoLimite);
        this.cliente = RestClient.builder()
                .baseUrl(urlBase)
                .defaultHeader("x-goog-api-key", clave)
                .requestFactory(factoria)
                .build();
        this.modelo = modelo;
        this.pausaBase = pausaBase;
    }

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public List<DescripcionSegmento> describir(List<ResumenSegmento> resumenes) {
        Solicitud cuerpo = new Solicitud(
                new Contenido(List.of(new Parte(INSTRUCCION))),
                List.of(new Contenido(List.of(new Parte(armarTexto(resumenes))))),
                new Configuracion(0.4, 4096, "application/json"));
        return interpretar(llamarConReintentos(cuerpo));
    }

    // ---------- Llamada y reintentos ----------

    private RespuestaGemini llamarConReintentos(Solicitud cuerpo) {
        for (int intento = 1; ; intento++) {
            try {
                return llamar(cuerpo);
            } catch (RestClientResponseException e) {
                int codigo = e.getStatusCode().value();
                if (!esReintentable(codigo) || intento >= MAXIMO_INTENTOS) {
                    throw new ProveedorIANoDisponibleException("Gemini respondió con el código " + codigo, e);
                }
                registro.warn("Gemini respondió {} (intento {} de {}): se reintenta", codigo, intento, MAXIMO_INTENTOS);
            } catch (ResourceAccessException e) {
                if (intento >= MAXIMO_INTENTOS) {
                    throw new ProveedorIANoDisponibleException(
                            "No se pudo conectar con Gemini o se agotó el tiempo de espera", e);
                }
                registro.warn("Sin respuesta de Gemini (intento {} de {}): se reintenta", intento, MAXIMO_INTENTOS);
            }
            pausar(intento);
        }
    }

    private RespuestaGemini llamar(Solicitud cuerpo) {
        return cliente.post()
                .uri("/models/{modelo}:generateContent", modelo)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(RespuestaGemini.class);
    }

    /** Se reintenta ante límite de uso (429) y fallos del servicio (5xx); un 400 o 404 no se arregla repitiendo. */
    private static boolean esReintentable(int codigo) {
        return codigo == 429 || codigo >= 500;
    }

    private void pausar(int intento) {
        try {
            Thread.sleep(pausaBase.toMillis() * intento); // espera creciente
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProveedorIANoDisponibleException("La espera entre reintentos fue interrumpida", e);
        }
    }

    // ---------- Construcción del texto y lectura de la respuesta ----------

    private static String armarTexto(List<ResumenSegmento> resumenes) {
        StringBuilder texto = new StringBuilder("Segmentos de clientes de una marca cosmética:\n");
        for (ResumenSegmento r : resumenes) {
            texto.append(String.format(Locale.ROOT,
                    "- id_segmento=%d; etiqueta=%s; clientes=%d (%.0f %% del total); compras promedio=%.1f; "
                            + "gasto total promedio=%.2f; días desde la última compra (promedio)=%.0f%n",
                    r.idSegmento(), r.etiqueta(), r.numClientes(), r.proporcion() * 100,
                    r.frecuenciaMedia(), r.valorTotalMedio(), r.recenciaMediaDias()));
        }
        return texto.toString();
    }

    private static List<DescripcionSegmento> interpretar(RespuestaGemini respuesta) {
        if (respuesta == null || respuesta.candidates() == null || respuesta.candidates().isEmpty()) {
            throw new ProveedorIANoDisponibleException("Gemini no devolvió candidatos (la respuesta pudo ser bloqueada)");
        }
        Candidato candidato = respuesta.candidates().get(0);
        String texto = candidato.content() == null || candidato.content().parts() == null ? null
                : candidato.content().parts().stream()
                        .map(Parte::text)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining());
        if (texto == null || texto.isBlank()) {
            throw new ProveedorIANoDisponibleException(
                    "La respuesta de Gemini no trae texto (motivo: " + candidato.finishReason() + ")");
        }

        try {
            Map<String, Object> raiz = JsonParserFactory.getJsonParser().parseMap(limpiar(texto));
            if (!(raiz.get("segmentos") instanceof List<?> lista)) {
                throw new ProveedorIANoDisponibleException("La respuesta no contiene la lista 'segmentos'");
            }
            List<DescripcionSegmento> resultado = new ArrayList<>();
            for (Object elemento : lista) {
                if (!(elemento instanceof Map<?, ?> segmento)
                        || !(segmento.get("id_segmento") instanceof Number id)
                        || !(segmento.get("descripcion") instanceof String descripcion)
                        || !(segmento.get("ideas_campana") instanceof List<?> ideas)) {
                    throw new ProveedorIANoDisponibleException(
                            "Un segmento de la respuesta tiene campos faltantes o de tipo incorrecto");
                }
                List<String> ideasTexto = ideas.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .limit(5)
                        .toList();
                resultado.add(new DescripcionSegmento(id.longValue(), descripcion.trim(), ideasTexto));
            }
            return resultado;
        } catch (IllegalArgumentException e) {
            throw new ProveedorIANoDisponibleException("La respuesta de Gemini no es un JSON válido", e);
        }
    }

    /** Por si el modelo envuelve el JSON en un bloque de Markdown. */
    private static String limpiar(String texto) {
        String limpio = texto.trim();
        if (limpio.startsWith("```")) {
            int salto = limpio.indexOf('\n');
            limpio = salto >= 0 ? limpio.substring(salto + 1) : limpio;
            if (limpio.endsWith("```")) {
                limpio = limpio.substring(0, limpio.length() - 3);
            }
        }
        return limpio.trim();
    }

    // ---------- Formato de la API de Google ----------

    record Parte(String text) {
    }

    record Contenido(List<Parte> parts) {
    }

    record Configuracion(double temperature, int maxOutputTokens, String responseMimeType) {
    }

    record Solicitud(Contenido systemInstruction, List<Contenido> contents, Configuracion generationConfig) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidato(Contenido content, String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaGemini(List<Candidato> candidates) {
    }
}