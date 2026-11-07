import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Genera un CSV sintético de compras con cuatro perfiles de clientes distintos.
 * Uso, desde la raíz del proyecto:  java datos/GeneradorDatos.java
 */
public class GeneradorDatos {

    record Perfil(String nombre, int clientes, int minCompras, int maxCompras,
                  double valorBase, int minDiasUltima, int maxDiasUltima) {
    }

    public static void main(String[] args) throws IOException {
        LocalDate referencia = LocalDate.of(2026, 11, 1);
        Random azar = new Random(42); // semilla fija: siempre genera el mismo archivo

        List<Perfil> perfiles = List.of(
                new Perfil("frecuentes", 120, 8, 14, 90, 1, 30),
                new Perfil("ocasionales", 200, 2, 4, 60, 60, 180),
                new Perfil("alto valor", 60, 3, 6, 350, 5, 90),
                new Perfil("inactivos", 150, 1, 3, 40, 250, 360));

        List<String> filas = new ArrayList<>();
        int cliente = 0;
        int compra = 0;
        for (Perfil p : perfiles) {
            for (int i = 0; i < p.clientes(); i++) {
                cliente++;
                int cantidad = p.minCompras() + azar.nextInt(p.maxCompras() - p.minCompras() + 1);
                int diasUltima = p.minDiasUltima() + azar.nextInt(p.maxDiasUltima() - p.minDiasUltima() + 1);
                for (int k = 0; k < cantidad; k++) {
                    int dias = (k == 0) ? diasUltima : diasUltima + 1 + azar.nextInt(Math.max(1, 365 - diasUltima));
                    LocalDate fecha = referencia.minusDays(dias);
                    double valor = p.valorBase() * (0.6 + azar.nextDouble() * 0.8);
                    compra++;
                    filas.add(String.format(Locale.ROOT, "P-%05d,C-%04d,%s,%.2f", compra, cliente, fecha, valor));
                }
            }
        }
        Collections.shuffle(filas, azar);

        List<String> salida = new ArrayList<>();
        salida.add("id_compra,id_cliente,fecha_compra,valor_compra");
        salida.addAll(filas);
        Path destino = Path.of("datos", "compras_sinteticas.csv");
        Files.write(destino, salida, StandardCharsets.UTF_8);

        System.out.println("Generado " + destino + " con " + compra + " compras de " + cliente + " clientes");
    }
}