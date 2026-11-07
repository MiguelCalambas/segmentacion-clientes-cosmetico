package com.usc.segmentacion.negocio.servicio;

import com.usc.segmentacion.negocio.entidad.IncidenciaCalidad;
import com.usc.segmentacion.negocio.modelo.FilaCruda;
import com.usc.segmentacion.negocio.modelo.FilaValida;
import com.usc.segmentacion.negocio.modelo.IncidenciaDetectada;
import com.usc.segmentacion.negocio.modelo.ResultadoValidacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCompras {

    private static final int LONGITUD_MAXIMA_ID = 50;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("9999999999.99");

    public ResultadoValidacion validar(List<FilaCruda> filas) {
        List<FilaValida> validas = new ArrayList<>();
        List<IncidenciaDetectada> incidencias = new ArrayList<>();
        Set<String> codigosVistos = new HashSet<>();

        for (FilaCruda f : filas) {
            if (vacio(f.idCompra()) || vacio(f.idCliente()) || vacio(f.fecha()) || vacio(f.valor())) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.CAMPO_VACIO,
                        "Fila " + f.fila() + ": hay campos obligatorios vacíos"));
                continue;
            }
            if (f.idCompra().length() > LONGITUD_MAXIMA_ID || f.idCliente().length() > LONGITUD_MAXIMA_ID) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.FORMATO_INVALIDO,
                        "Fila " + f.fila() + ": id_compra o id_cliente superan los 50 caracteres"));
                continue;
            }

            LocalDate fecha;
            try {
                fecha = LocalDate.parse(f.fecha());
            } catch (DateTimeParseException e) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.FORMATO_INVALIDO,
                        "Fila " + f.fila() + ": la fecha '" + f.fecha() + "' no tiene el formato AAAA-MM-DD"));
                continue;
            }

            BigDecimal valor;
            try {
                valor = new BigDecimal(f.valor());
            } catch (NumberFormatException e) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.FORMATO_INVALIDO,
                        "Fila " + f.fila() + ": el valor '" + f.valor() + "' no es un número (use punto decimal)"));
                continue;
            }
            if (valor.signum() <= 0 || valor.compareTo(VALOR_MAXIMO) > 0) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.FORMATO_INVALIDO,
                        "Fila " + f.fila() + ": el valor debe ser mayor que 0 y menor o igual a 9999999999.99"));
                continue;
            }

            if (!codigosVistos.add(f.idCompra())) {
                incidencias.add(new IncidenciaDetectada(IncidenciaCalidad.DUPLICADO,
                        "Fila " + f.fila() + ": id_compra '" + f.idCompra() + "' repetido"));
                continue;
            }

            validas.add(new FilaValida(f.idCompra(), f.idCliente(), fecha, valor));
        }
        return new ResultadoValidacion(validas, incidencias);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}