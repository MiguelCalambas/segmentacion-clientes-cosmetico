package com.usc.segmentacion.negocio.servicio;

import com.usc.segmentacion.negocio.contrato.Segmentador;
import com.usc.segmentacion.negocio.entidad.Asignacion;
import com.usc.segmentacion.negocio.entidad.Ejecucion;
import com.usc.segmentacion.negocio.entidad.Segmento;
import com.usc.segmentacion.negocio.excepcion.DatosInsuficientesException;
import com.usc.segmentacion.negocio.excepcion.RecursoNoEncontradoException;
import com.usc.segmentacion.negocio.modelo.GrupoEtiquetado;
import com.usc.segmentacion.negocio.modelo.ResultadoSegmentacion;
import com.usc.segmentacion.negocio.modelo.VariablesCliente;
import com.usc.segmentacion.negocio.repositorio.AsignacionRepositorio;
import com.usc.segmentacion.negocio.repositorio.CompraRepositorio;
import com.usc.segmentacion.negocio.repositorio.EjecucionRepositorio;
import com.usc.segmentacion.negocio.repositorio.ResumenCompras;
import com.usc.segmentacion.negocio.repositorio.SegmentoRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcesadorEjecucion {

    private final EjecucionRepositorio ejecuciones;
    private final CompraRepositorio compras;
    private final SegmentoRepositorio segmentos;
    private final AsignacionRepositorio asignaciones;
    private final Segmentador segmentador;
    private final EtiquetadorSegmentos etiquetador;

    public ProcesadorEjecucion(EjecucionRepositorio ejecuciones, CompraRepositorio compras,
                               SegmentoRepositorio segmentos, AsignacionRepositorio asignaciones,
                               Segmentador segmentador, EtiquetadorSegmentos etiquetador) {
        this.ejecuciones = ejecuciones;
        this.compras = compras;
        this.segmentos = segmentos;
        this.asignaciones = asignaciones;
        this.segmentador = segmentador;
        this.etiquetador = etiquetador;
    }

    @Transactional
    public void procesar(Long idEjecucion) {
        Ejecucion ejecucion = ejecuciones.findById(idEjecucion)
                .orElseThrow(() -> new RecursoNoEncontradoException("La ejecución " + idEjecucion + " no existe"));

        List<ResumenCompras> resumen = compras.resumenPorCliente(ejecucion.getArchivo().getId());
        if (resumen.isEmpty()) {
            throw new DatosInsuficientesException("El archivo no tiene compras para segmentar");
        }

        // La recencia se mide contra la fecha de la compra más reciente del archivo,
        // así el resultado no cambia según el día en que se ejecute.
        LocalDate referencia = resumen.stream()
                .map(ResumenCompras::getUltimaCompra)
                .max(Comparator.naturalOrder())
                .get();

        List<VariablesCliente> variables = resumen.stream()
                .map(r -> new VariablesCliente(r.getIdCliente(), r.getFrecuencia(),
                        r.getValorTotal().doubleValue(),
                        ChronoUnit.DAYS.between(r.getUltimaCompra(), referencia)))
                .toList();

        ResultadoSegmentacion resultado = segmentador.segmentar(variables, leerK(ejecucion.getParametros()));

        for (GrupoEtiquetado g : etiquetador.etiquetar(resultado.grupos())) {
            Segmento segmento = new Segmento(ejecucion, g.etiqueta(), g.grupo().idsClientes().size(),
                    decimal(g.grupo().frecuenciaMedia()),
                    decimal(g.grupo().valorTotalMedio()),
                    decimal(g.grupo().recenciaMediaDias()));
            segmento.setDescripcion(g.descripcion());
            segmento = segmentos.save(segmento);

            Long idSegmento = segmento.getId();
            asignaciones.saveAll(g.grupo().idsClientes().stream()
                    .map(idCliente -> new Asignacion(idSegmento, idCliente))
                    .toList());
        }
        ejecucion.completar();
    }

    @Transactional
    public void marcarFallida(Long idEjecucion, String mensaje) {
        ejecuciones.findById(idEjecucion).ifPresent(e -> e.fallar(mensaje));
    }

    private static int leerK(String parametros) {
        if (parametros != null && parametros.startsWith("k=")) {
            return Integer.parseInt(parametros.substring(2).trim());
        }
        return EjecucionServicio.K_POR_DEFECTO;
    }

    private static BigDecimal decimal(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }
}