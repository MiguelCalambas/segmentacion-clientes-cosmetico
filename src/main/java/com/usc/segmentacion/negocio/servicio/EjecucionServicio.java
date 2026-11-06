package com.usc.segmentacion.negocio.servicio;

import com.usc.segmentacion.negocio.contrato.LanzadorEjecuciones;
import com.usc.segmentacion.negocio.entidad.ArchivoCarga;
import com.usc.segmentacion.negocio.entidad.Ejecucion;
import com.usc.segmentacion.negocio.entidad.Segmento;
import com.usc.segmentacion.negocio.excepcion.DatosInsuficientesException;
import com.usc.segmentacion.negocio.excepcion.EjecucionNoCompletadaException;
import com.usc.segmentacion.negocio.excepcion.ParametroInvalidoException;
import com.usc.segmentacion.negocio.excepcion.RecursoNoEncontradoException;
import com.usc.segmentacion.negocio.modelo.ConsultaSegmentos;
import com.usc.segmentacion.negocio.modelo.EstadoEjecucion;
import com.usc.segmentacion.negocio.modelo.SegmentoConsultado;
import com.usc.segmentacion.negocio.repositorio.ArchivoCargaRepositorio;
import com.usc.segmentacion.negocio.repositorio.CompraRepositorio;
import com.usc.segmentacion.negocio.repositorio.EjecucionRepositorio;
import com.usc.segmentacion.negocio.repositorio.SegmentoRepositorio;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class EjecucionServicio {

    public static final int K_POR_DEFECTO = 4;
    private static final int K_MAXIMO = 10;
    private static final List<String> VARIABLES = List.of("frecuencia", "valor_total", "recencia_dias");

    private final ArchivoCargaRepositorio archivos;
    private final EjecucionRepositorio ejecuciones;
    private final CompraRepositorio compras;
    private final SegmentoRepositorio segmentos;
    private final LanzadorEjecuciones lanzador;

    public EjecucionServicio(ArchivoCargaRepositorio archivos, EjecucionRepositorio ejecuciones,
                             CompraRepositorio compras, SegmentoRepositorio segmentos,
                             LanzadorEjecuciones lanzador) {
        this.archivos = archivos;
        this.ejecuciones = ejecuciones;
        this.compras = compras;
        this.segmentos = segmentos;
        this.lanzador = lanzador;
    }

    @Transactional
    public EstadoEjecucion crear(Long idArchivo, String tecnica, List<String> variables, Integer k) {
        ArchivoCarga archivo = archivos.findById(idArchivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El archivo " + idArchivo + " no existe"));
        validarTecnica(tecnica);
        validarVariables(variables);

        int grupos = k == null ? K_POR_DEFECTO : k;
        if (grupos < 2 || grupos > K_MAXIMO) {
            throw new ParametroInvalidoException("El número de grupos (k) debe estar entre 2 y " + K_MAXIMO);
        }
        long clientes = compras.contarClientes(idArchivo);
        if (clientes < grupos) {
            throw new DatosInsuficientesException("El archivo tiene " + clientes
                    + " clientes, insuficientes para formar " + grupos + " grupos");
        }

        Ejecucion ejecucion = ejecuciones.save(
                new Ejecucion(archivo, "kmeans", String.join(",", VARIABLES), "k=" + grupos));
        Long idEjecucion = ejecucion.getId();

        // Se lanza en segundo plano solo después de confirmar (commit) la transacción,
        // para que el proceso encuentre la ejecución ya guardada.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                lanzador.lanzar(idEjecucion);
            }
        });
        return aEstado(ejecucion);
    }

    @Transactional(readOnly = true)
    public EstadoEjecucion obtener(Long idEjecucion) {
        return aEstado(buscar(idEjecucion));
    }

    @Transactional(readOnly = true)
    public ConsultaSegmentos consultar(Long idEjecucion) {
        Ejecucion ejecucion = buscar(idEjecucion);
        if (!Ejecucion.COMPLETADA.equals(ejecucion.getEstado())) {
            throw new EjecucionNoCompletadaException(ejecucion.getEstado(), ejecucion.getMensajeError());
        }
        List<Segmento> lista = segmentos.findByEjecucionIdOrderByIdAsc(idEjecucion);
        int total = lista.stream().mapToInt(Segmento::getNumClientes).sum();

        List<SegmentoConsultado> salida = lista.stream()
                .map(s -> new SegmentoConsultado(s.getId(), s.getEtiqueta(), s.getNumClientes(),
                        total == 0 ? 0 : Math.round(s.getNumClientes() * 100.0 / total) / 100.0,
                        s.getFrecuenciaMedia(), s.getValorTotalMedio(), s.getRecenciaMediaDias(),
                        s.getDescripcion()))
                .toList();
        return new ConsultaSegmentos(idEjecucion, ejecucion.getEstado(), salida);
    }

    private Ejecucion buscar(Long idEjecucion) {
        return ejecuciones.findById(idEjecucion)
                .orElseThrow(() -> new RecursoNoEncontradoException("La ejecución " + idEjecucion + " no existe"));
    }

    private static void validarTecnica(String tecnica) {
        if (tecnica != null && !"kmeans".equalsIgnoreCase(tecnica.trim())) {
            throw new ParametroInvalidoException(
                    "Técnica no soportada: '" + tecnica + "'. La única disponible es kmeans");
        }
    }

    private static void validarVariables(List<String> variables) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        for (String v : variables) {
            if (!VARIABLES.contains(v)) {
                throw new ParametroInvalidoException(
                        "Variable inválida: '" + v + "'. Las válidas son: " + String.join(", ", VARIABLES));
            }
        }
        if (!new HashSet<>(variables).containsAll(VARIABLES)) {
            throw new ParametroInvalidoException(
                    "En esta versión la segmentación usa las tres variables: " + String.join(", ", VARIABLES));
        }
    }

    private static EstadoEjecucion aEstado(Ejecucion e) {
        return new EstadoEjecucion(e.getId(), e.getArchivo().getId(), e.getTecnica(),
                e.getVariablesUsadas(), e.getParametros(), e.getEstado(), e.getMensajeError());
    }
}