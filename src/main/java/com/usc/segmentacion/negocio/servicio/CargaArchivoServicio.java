package com.usc.segmentacion.negocio.servicio;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usc.segmentacion.negocio.contrato.LectorCompras;
import com.usc.segmentacion.negocio.entidad.ArchivoCarga;
import com.usc.segmentacion.negocio.entidad.Cliente;
import com.usc.segmentacion.negocio.entidad.Compra;
import com.usc.segmentacion.negocio.entidad.IncidenciaCalidad;
import com.usc.segmentacion.negocio.excepcion.SinRegistrosValidosException;
import com.usc.segmentacion.negocio.modelo.FilaCruda;
import com.usc.segmentacion.negocio.modelo.FilaValida;
import com.usc.segmentacion.negocio.modelo.IncidenciaDetectada;
import com.usc.segmentacion.negocio.modelo.ResultadoCarga;
import com.usc.segmentacion.negocio.modelo.ResultadoValidacion;
import com.usc.segmentacion.negocio.repositorio.ArchivoCargaRepositorio;
import com.usc.segmentacion.negocio.repositorio.ClienteRepositorio;
import com.usc.segmentacion.negocio.repositorio.CompraRepositorio;
import com.usc.segmentacion.negocio.repositorio.IncidenciaCalidadRepositorio;

@Service
public class CargaArchivoServicio {

    private static final int MAXIMO_DETALLES = 20;
    private static final int LONGITUD_DETALLE = 500;

    private final LectorCompras lector;
    private final ValidadorCompras validador;
    private final ArchivoCargaRepositorio archivos;
    private final IncidenciaCalidadRepositorio incidencias;
    private final ClienteRepositorio clientes;
    private final CompraRepositorio compras;

    public CargaArchivoServicio(LectorCompras lector, ValidadorCompras validador,
                                ArchivoCargaRepositorio archivos,
                                IncidenciaCalidadRepositorio incidencias,
                                ClienteRepositorio clientes, CompraRepositorio compras) {
        this.lector = lector;
        this.validador = validador;
        this.archivos = archivos;
        this.incidencias = incidencias;
        this.clientes = clientes;
        this.compras = compras;
    }

    @Transactional
    public ResultadoCarga cargar(String nombreArchivo, InputStream contenido) {
        List<FilaCruda> filas = lector.leer(contenido);
        ResultadoValidacion resultado = validador.validar(filas);

        if (resultado.validas().isEmpty()) {
            throw new SinRegistrosValidosException(filas.isEmpty()
                    ? "El archivo no contiene registros"
                    : "El archivo no contiene registros válidos: se encontraron "
                        + resultado.incidencias().size() + " incidencias");
        }

        ArchivoCarga archivo = new ArchivoCarga(nombreArchivo);
        archivo.setRegistrosLeidos(filas.size());
        archivo.setEstado(resultado.incidencias().isEmpty()
                ? ArchivoCarga.VALIDADO
                : ArchivoCarga.VALIDADO_CON_OBSERVACIONES);
        archivo = archivos.save(archivo);

        guardarIncidencias(archivo, resultado.incidencias());
        guardarCompras(archivo, resultado.validas());

        Map<String, Long> porTipo = resultado.incidencias().stream()
                .collect(Collectors.groupingBy(IncidenciaDetectada::tipo,
                        LinkedHashMap::new, Collectors.counting()));
        List<String> detalles = resultado.incidencias().stream()
                .map(IncidenciaDetectada::detalle)
                .limit(MAXIMO_DETALLES)
                .toList();

        return new ResultadoCarga(archivo.getId(), nombreArchivo, archivo.getEstado(),
                filas.size(), resultado.validas().size(), porTipo, detalles);
    }

    private void guardarIncidencias(ArchivoCarga archivo, List<IncidenciaDetectada> detectadas) {
        List<IncidenciaCalidad> lista = detectadas.stream()
                .map(i -> new IncidenciaCalidad(archivo, i.tipo(), recortar(i.detalle())))
                .toList();
        incidencias.saveAll(lista);
    }

    private void guardarCompras(ArchivoCarga archivo, List<FilaValida> validas) {
        Set<String> idsClientes = validas.stream()
                .map(FilaValida::idCliente)
                .collect(Collectors.toSet());

        Map<String, Cliente> porId = new HashMap<>();
        clientes.findAllById(idsClientes).forEach(c -> porId.put(c.getId(), c));

        List<Cliente> nuevos = idsClientes.stream()
                .filter(id -> !porId.containsKey(id))
                .map(Cliente::new)
                .toList();
        clientes.saveAll(nuevos).forEach(c -> porId.put(c.getId(), c));

        List<Compra> lista = validas.stream()
                .map(f -> new Compra(archivo, porId.get(f.idCliente()),
                        f.idCompra(), f.fecha(), f.valor()))
                .toList();
        compras.saveAll(lista);
    }

    private static String recortar(String texto) {
        return texto.length() <= LONGITUD_DETALLE ? texto : texto.substring(0, LONGITUD_DETALLE);
    }
}