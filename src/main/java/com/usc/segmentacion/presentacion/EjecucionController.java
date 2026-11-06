package com.usc.segmentacion.presentacion;

import com.usc.segmentacion.negocio.modelo.EstadoEjecucion;
import com.usc.segmentacion.negocio.servicio.DescripcionServicio;
import com.usc.segmentacion.negocio.servicio.EjecucionServicio;
import com.usc.segmentacion.presentacion.dto.RespuestaDescripciones;
import com.usc.segmentacion.presentacion.dto.RespuestaEjecucion;
import com.usc.segmentacion.presentacion.dto.RespuestaError;
import com.usc.segmentacion.presentacion.dto.RespuestaSegmentos;
import com.usc.segmentacion.presentacion.dto.SolicitudEjecucion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ejecuciones", description = "Segmentación de clientes con K-means y consulta de segmentos")
@RestController
@RequestMapping("/api/v1/ejecuciones")
public class EjecucionController {

    private final EjecucionServicio servicio;
    private final DescripcionServicio descripciones;

    public EjecucionController(EjecucionServicio servicio, DescripcionServicio descripciones) {
        this.servicio = servicio;
        this.descripciones = descripciones;
    }

    @Operation(summary = "Inicia una segmentación",
            description = "Crea la ejecución en estado en_proceso y la procesa en segundo plano. "
                    + "Consulte el estado con GET /api/v1/ejecuciones/{id}. "
                    + "Técnica disponible: kmeans. Número de grupos (k): de 2 a 10, 4 por defecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Ejecución aceptada"),
            @ApiResponse(responseCode = "404", description = "El archivo no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "422", description = "Parámetros inválidos o datos insuficientes",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RespuestaEjecucion crear(@Valid @RequestBody SolicitudEjecucion solicitud) {
        Integer k = solicitud.parametros() == null ? null : solicitud.parametros().k();
        EstadoEjecucion estado = servicio.crear(solicitud.idArchivo(), solicitud.tecnica(),
                solicitud.variables(), k);
        return RespuestaEjecucion.resumida(estado);
    }

    @Operation(summary = "Consulta el estado de una ejecución",
            description = "Estados posibles: en_proceso, completada y fallida (con su mensaje de error).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de la ejecución"),
            @ApiResponse(responseCode = "404", description = "La ejecución no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping("/{id}")
    public RespuestaEjecucion obtener(
            @Parameter(description = "Identificador de la ejecución") @PathVariable("id") Long id) {
        return RespuestaEjecucion.completa(servicio.obtener(id));
    }

    @Operation(summary = "Obtiene los segmentos de una ejecución completada",
            description = "Cada segmento incluye su etiqueta, el número de clientes, sus promedios, "
                    + "la descripción con el criterio de la etiqueta y, si ya se generaron, "
                    + "las descripciones e ideas de campaña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Segmentos de la ejecución"),
            @ApiResponse(responseCode = "404", description = "La ejecución no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "La ejecución aún no termina o falló",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping("/{id}/segmentos")
    public RespuestaSegmentos segmentos(
            @Parameter(description = "Identificador de la ejecución") @PathVariable("id") Long id) {
        return RespuestaSegmentos.desde(servicio.consultar(id));
    }

    @Operation(summary = "Genera descripciones e ideas de campaña para cada segmento",
            description = "Usa el proveedor configurado y, si no está disponible, una plantilla de respaldo. "
                    + "Al proveedor solo se le envían resúmenes agregados por segmento, nunca datos de clientes. "
                    + "Las ideas son orientativas y no garantizan resultados comerciales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Descripciones generadas y guardadas"),
            @ApiResponse(responseCode = "404", description = "La ejecución no existe",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "La ejecución aún no termina o falló",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/descripciones")
    public RespuestaDescripciones describir(
            @Parameter(description = "Identificador de la ejecución") @PathVariable("id") Long id) {
        return RespuestaDescripciones.desde(descripciones.describir(id));
    }
}