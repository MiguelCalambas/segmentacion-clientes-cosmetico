package com.usc.segmentacion.negocio.servicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;
import com.usc.segmentacion.negocio.entidad.Ejecucion;
import com.usc.segmentacion.negocio.entidad.Segmento;
import com.usc.segmentacion.negocio.excepcion.EjecucionNoCompletadaException;
import com.usc.segmentacion.negocio.excepcion.RecursoNoEncontradoException;
import com.usc.segmentacion.negocio.modelo.DescripcionSegmento;
import com.usc.segmentacion.negocio.modelo.DescripcionesGeneradas;
import com.usc.segmentacion.negocio.modelo.ResultadoDescripciones;
import com.usc.segmentacion.negocio.modelo.ResumenSegmento;
import com.usc.segmentacion.negocio.modelo.SegmentoDescrito;
import com.usc.segmentacion.negocio.repositorio.EjecucionRepositorio;
import com.usc.segmentacion.negocio.repositorio.SegmentoRepositorio;

@Service
public class DescripcionServicio {

    private final EjecucionRepositorio ejecuciones;
    private final SegmentoRepositorio segmentos;
    private final AsistenteConRespaldo asistente;
    private final TransactionTemplate transaccion;

    public DescripcionServicio(EjecucionRepositorio ejecuciones, SegmentoRepositorio segmentos,
                               AsistenteConRespaldo asistente, TransactionTemplate transaccion) {
        this.ejecuciones = ejecuciones;
        this.segmentos = segmentos;
        this.asistente = asistente;
        this.transaccion = transaccion;
    }

    public ResultadoDescripciones describir(Long idEjecucion) {
        // 1. Leer los resúmenes (transacción corta)
        List<ResumenSegmento> resumenes = transaccion.execute(estado -> leerResumenes(idEjecucion));
        // 2. Llamar al asistente SIN transacción abierta: puede tardar varios segundos
        DescripcionesGeneradas generadas = asistente.describir(resumenes);
        // 3. Guardar el resultado (transacción corta)
        return transaccion.execute(estado -> guardar(idEjecucion, generadas));
    }

    private List<ResumenSegmento> leerResumenes(Long idEjecucion) {
        Ejecucion ejecucion = ejecuciones.findById(idEjecucion)
                .orElseThrow(() -> new RecursoNoEncontradoException("La ejecución " + idEjecucion + " no existe"));
        if (!Ejecucion.COMPLETADA.equals(ejecucion.getEstado())) {
            throw new EjecucionNoCompletadaException(ejecucion.getEstado(), ejecucion.getMensajeError());
        }
        List<Segmento> lista = segmentos.findByEjecucionIdOrderByIdAsc(idEjecucion);
        int total = lista.stream().mapToInt(Segmento::getNumClientes).sum();
        return lista.stream()
                .map(s -> new ResumenSegmento(s.getId(), s.getEtiqueta(), s.getNumClientes(),
                        total == 0 ? 0 : (double) s.getNumClientes() / total,
                        s.getFrecuenciaMedia().doubleValue(),
                        s.getValorTotalMedio().doubleValue(),
                        s.getRecenciaMediaDias().doubleValue()))
                .toList();
    }

    private ResultadoDescripciones guardar(Long idEjecucion, DescripcionesGeneradas generadas) {
        Map<Long, Segmento> porId = segmentos.findByEjecucionIdOrderByIdAsc(idEjecucion).stream()
                .collect(Collectors.toMap(Segmento::getId, s -> s));

        List<SegmentoDescrito> salida = new ArrayList<>();
        for (DescripcionSegmento d : generadas.descripciones()) {
            Segmento segmento = porId.get(d.idSegmento());
            segmento.registrarDescripcionIa(d.descripcion(), String.join("\n", d.ideasCampana()),
                    generadas.proveedor());
            salida.add(new SegmentoDescrito(segmento.getId(), segmento.getEtiqueta(),
                    d.descripcion(), d.ideasCampana()));
        }
        boolean generadoPorIa = !AsistenteDescriptivo.NOMBRE_PLANTILLA.equals(generadas.proveedor());
        return new ResultadoDescripciones(idEjecucion, generadas.proveedor(), generadas.respaldo(),
                generadoPorIa, salida);
    }
}