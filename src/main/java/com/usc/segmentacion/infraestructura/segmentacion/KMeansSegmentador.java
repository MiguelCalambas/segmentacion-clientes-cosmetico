package com.usc.segmentacion.infraestructura.segmentacion;

import com.usc.segmentacion.negocio.contrato.Segmentador;
import com.usc.segmentacion.negocio.excepcion.DatosInsuficientesException;
import com.usc.segmentacion.negocio.excepcion.ParametroInvalidoException;
import com.usc.segmentacion.negocio.modelo.GrupoSegmento;
import com.usc.segmentacion.negocio.modelo.ResultadoSegmentacion;
import com.usc.segmentacion.negocio.modelo.VariablesCliente;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.springframework.stereotype.Component;

@Component
public class KMeansSegmentador implements Segmentador {

    private static final int MAXIMO_ITERACIONES = 100;
    private static final int SEMILLA = 42;

    @Override
    public ResultadoSegmentacion segmentar(List<VariablesCliente> clientes, int numeroGrupos) {
        if (numeroGrupos < 2) {
            throw new ParametroInvalidoException("El número de grupos (k) debe ser al menos 2");
        }
        if (clientes.size() < numeroGrupos) {
            throw new DatosInsuficientesException("No hay suficientes clientes (" + clientes.size()
                    + ") para formar " + numeroGrupos + " grupos");
        }

        // 1. Normalizar: cada variable queda con promedio 0 y desviación 1
        double[] medias = new double[3];
        double[] desviaciones = new double[3];
        calcularEstadisticas(clientes, medias, desviaciones);

        List<ClientePunto> puntos = new ArrayList<>();
        for (VariablesCliente c : clientes) {
            puntos.add(new ClientePunto(c, new double[] {
                    (c.frecuencia() - medias[0]) / desviaciones[0],
                    (c.valorTotal() - medias[1]) / desviaciones[1],
                    (c.recenciaDias() - medias[2]) / desviaciones[2]}));
        }

        // 2. Agrupar con K-means++ (semilla fija para que el resultado sea reproducible)
        KMeansPlusPlusClusterer<ClientePunto> algoritmo = new KMeansPlusPlusClusterer<>(
                numeroGrupos, MAXIMO_ITERACIONES, new EuclideanDistance(), new JDKRandomGenerator(SEMILLA));
        List<CentroidCluster<ClientePunto>> clusters = algoritmo.cluster(puntos);

        // 3. Resumir cada grupo con los valores originales (sin normalizar)
        List<GrupoSegmento> grupos = new ArrayList<>();
        for (CentroidCluster<ClientePunto> cluster : clusters) {
            if (!cluster.getPoints().isEmpty()) {
                grupos.add(resumir(cluster.getPoints()));
            }
        }

        // 4. Ordenar de mayor a menor valor total medio, para que los índices sean estables
        grupos.sort(Comparator.comparingDouble(GrupoSegmento::valorTotalMedio).reversed());
        List<GrupoSegmento> ordenados = new ArrayList<>();
        for (int i = 0; i < grupos.size(); i++) {
            GrupoSegmento g = grupos.get(i);
            ordenados.add(new GrupoSegmento(i + 1, g.idsClientes(),
                    g.frecuenciaMedia(), g.valorTotalMedio(), g.recenciaMediaDias()));
        }
        return new ResultadoSegmentacion(ordenados);
    }

    private static void calcularEstadisticas(List<VariablesCliente> clientes, double[] medias, double[] desviaciones) {
        int n = clientes.size();
        for (VariablesCliente c : clientes) {
            medias[0] += c.frecuencia();
            medias[1] += c.valorTotal();
            medias[2] += c.recenciaDias();
        }
        for (int i = 0; i < 3; i++) {
            medias[i] /= n;
        }
        for (VariablesCliente c : clientes) {
            desviaciones[0] += Math.pow(c.frecuencia() - medias[0], 2);
            desviaciones[1] += Math.pow(c.valorTotal() - medias[1], 2);
            desviaciones[2] += Math.pow(c.recenciaDias() - medias[2], 2);
        }
        for (int i = 0; i < 3; i++) {
            desviaciones[i] = Math.sqrt(desviaciones[i] / n);
            if (desviaciones[i] < 1e-9) {
                desviaciones[i] = 1.0; // variable constante: evita dividir por cero
            }
        }
    }

    private static GrupoSegmento resumir(List<ClientePunto> miembros) {
        double frecuencia = 0;
        double valor = 0;
        double recencia = 0;
        List<String> ids = new ArrayList<>();
        for (ClientePunto p : miembros) {
            VariablesCliente c = p.cliente();
            frecuencia += c.frecuencia();
            valor += c.valorTotal();
            recencia += c.recenciaDias();
            ids.add(c.idCliente());
        }
        int n = miembros.size();
        return new GrupoSegmento(0, ids, frecuencia / n, valor / n, recencia / n);
    }

    /** Un cliente convertido en un punto del espacio de tres variables normalizadas. */
    private record ClientePunto(VariablesCliente cliente, double[] punto) implements Clusterable {
        @Override
        public double[] getPoint() {
            return punto;
        }
    }
}