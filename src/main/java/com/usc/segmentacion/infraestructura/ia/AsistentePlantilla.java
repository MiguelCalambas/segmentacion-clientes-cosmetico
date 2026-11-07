package com.usc.segmentacion.infraestructura.ia;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

/** Descripciones por reglas fijas: no usa IA, no necesita red ni clave. Es el respaldo del sistema. */
@Component("asistentePlantilla")
public class AsistentePlantilla implements AsistenteDescriptivo {

    private static final Map<String, List<String>> IDEAS = Map.of(
            "Alto valor", List.of(
                    "Atención personalizada y acceso anticipado a lanzamientos",
                    "Muestras exclusivas junto con su próxima compra",
                    "Beneficios por gasto acumulado"),
            "Frecuentes", List.of(
                    "Programa de fidelidad con puntos por compra",
                    "Ofertas de recompra de productos de uso continuo",
                    "Invitación a dejar reseñas y recomendar a otras personas"),
            "Ocasionales", List.of(
                    "Recordatorios con productos relacionados a su última compra",
                    "Promociones de tiempo limitado para acercarlos",
                    "Combos de iniciación para probar nuevas líneas"),
            "Inactivos", List.of(
                    "Campaña de reactivación con un incentivo claro",
                    "Mensaje de reencuentro con las novedades del catálogo",
                    "Encuesta breve para entender por qué dejaron de comprar"));

    private static final List<String> IDEAS_GENERALES = List.of(
            "Comunicaciones periódicas con las novedades del catálogo",
            "Promoción de bienvenida a una nueva categoría de productos");

    @Override
    public String nombre() {
        return NOMBRE_PLANTILLA;
    }

    @Override
    public List<DescripcionSegmento> describir(List<ResumenSegmento> resumenes) {
        return resumenes.stream()
                .map(r -> new DescripcionSegmento(r.idSegmento(), texto(r), ideas(r.etiqueta())))
                .toList();
    }

    private static String texto(ResumenSegmento r) {
        return String.format(Locale.ROOT,
                "Grupo «%s» con %d clientes (%.0f %% del total). En promedio compran %.1f veces, "
                        + "gastan %.2f en total y su última compra fue hace %.0f días.",
                r.etiqueta(), r.numClientes(), r.proporcion() * 100,
                r.frecuenciaMedia(), r.valorTotalMedio(), r.recenciaMediaDias());
    }

    private static List<String> ideas(String etiqueta) {
        // "Ocasionales 2" usa las mismas ideas que "Ocasionales"
        String base = etiqueta.replaceAll("\\s+\\d+$", "");
        return IDEAS.getOrDefault(base, IDEAS_GENERALES);
    }
}