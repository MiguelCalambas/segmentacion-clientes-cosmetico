package com.usc.segmentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.usc.segmentacion.negocio.entidad.ArchivoCarga;
import com.usc.segmentacion.negocio.entidad.Cliente;
import com.usc.segmentacion.negocio.entidad.Compra;
import com.usc.segmentacion.negocio.modelo.ConsultaSegmentos;
import com.usc.segmentacion.negocio.modelo.EstadoEjecucion;
import com.usc.segmentacion.negocio.modelo.SegmentoConsultado;
import com.usc.segmentacion.negocio.repositorio.ArchivoCargaRepositorio;
import com.usc.segmentacion.negocio.repositorio.ClienteRepositorio;
import com.usc.segmentacion.negocio.repositorio.CompraRepositorio;
import com.usc.segmentacion.negocio.servicio.EjecucionServicio;
import com.usc.segmentacion.negocio.servicio.ProcesadorEjecucion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SegmentacionIntegracionTest {

    @Autowired
    private ArchivoCargaRepositorio archivos;
    @Autowired
    private ClienteRepositorio clientes;
    @Autowired
    private CompraRepositorio compras;
    @Autowired
    private EjecucionServicio ejecuciones;
    @Autowired
    private ProcesadorEjecucion procesador;

    private void crearClientes(ArchivoCarga archivo, String prefijo, int cantidad,
                               int comprasBase, double valorBase, int diasUltima) {
        LocalDate referencia = LocalDate.of(2026, 11, 1);
        for (int i = 1; i <= cantidad; i++) {
            Cliente cliente = clientes.save(new Cliente(prefijo + "-" + i));
            int numeroCompras = comprasBase + (i % 3);
            for (int k = 0; k < numeroCompras; k++) {
                BigDecimal valor = BigDecimal.valueOf(valorBase * (1 + (i % 5) * 0.05))
                        .setScale(2, RoundingMode.HALF_UP);
                compras.save(new Compra(archivo, cliente, prefijo + "-" + i + "-" + k,
                        referencia.minusDays(diasUltima + (i % 7) + k * 10L), valor));
            }
        }
    }

    @Test
    void segmentaLosClientesGuardadosEnLaBaseDeDatos() {
        ArchivoCarga archivo = archivos.save(new ArchivoCarga("prueba-integracion.csv"));
        crearClientes(archivo, "A", 20, 10, 100.0, 5);
        crearClientes(archivo, "B", 20, 2, 40.0, 200);
        crearClientes(archivo, "C", 20, 4, 400.0, 60);

        EstadoEjecucion estado = ejecuciones.crear(archivo.getId(), "kmeans", null, 3);
        assertEquals("en_proceso", estado.estado());

        procesador.procesar(estado.idEjecucion());
        ConsultaSegmentos consulta = ejecuciones.consultar(estado.idEjecucion());

        assertEquals("completada", consulta.estado());
        assertEquals(3, consulta.segmentos().size());
        assertEquals(60, consulta.segmentos().stream().mapToInt(SegmentoConsultado::numClientes).sum());
        consulta.segmentos().forEach(s -> assertFalse(s.descripcion().isBlank()));
    }
}