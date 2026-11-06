package com.usc.segmentacion.negocio.servicio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;
import com.usc.segmentacion.negocio.excepcion.ProveedorIANoDisponibleException;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.DescripcionesGeneradas;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;

@Component
public class AsistenteConRespaldo {

    private static final Logger registro = LoggerFactory.getLogger(AsistenteConRespaldo.class);

    private final AsistenteDescriptivo principal;
    private final AsistenteDescriptivo respaldo;

    public AsistenteConRespaldo(@Qualifier("asistenteConfigurado") AsistenteDescriptivo principal,
                                @Qualifier("asistentePlantilla") AsistenteDescriptivo respaldo) {
        this.principal = principal;
        this.respaldo = respaldo;
    }

    public DescripcionesGeneradas describir(List<ResumenSegmento> resumenes) {
        try {
            List<DescripcionSegmento> generadas = principal.describir(resumenes);
            validar(resumenes, generadas);
            return new DescripcionesGeneradas(principal.nombre(), false, generadas);
        } catch (ProveedorIANoDisponibleException e) {
            registro.warn("El proveedor '{}' no pudo responder ({}); se usa la plantilla",
                    principal.nombre(), e.getMessage());
            boolean esRespaldo = !principal.nombre().equals(respaldo.nombre());
            return new DescripcionesGeneradas(respaldo.nombre(), esRespaldo, respaldo.describir(resumenes));
        }
    }

    /** Una respuesta incompleta se trata como un fallo del proveedor (RNF-05). */
    private static void validar(List<ResumenSegmento> resumenes, List<DescripcionSegmento> generadas) {
        if (generadas == null) {
            throw new ProveedorIANoDisponibleException("El proveedor no devolvió descripciones");
        }
        Set<Long> esperados = resumenes.stream().map(ResumenSegmento::idSegmento).collect(Collectors.toSet());
        Set<Long> recibidos = new HashSet<>();
        for (DescripcionSegmento d : generadas) {
            if (d.descripcion() == null || d.descripcion().isBlank() || d.ideasCampana() == null) {
                throw new ProveedorIANoDisponibleException("La respuesta trae una descripción vacía o incompleta");
            }
            recibidos.add(d.idSegmento());
        }
        if (!recibidos.equals(esperados)) {
            throw new ProveedorIANoDisponibleException("La respuesta no cubre exactamente los segmentos enviados");
        }
    }
}