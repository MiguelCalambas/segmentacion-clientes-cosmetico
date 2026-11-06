package com.usc.segmentacion.presentacion;

import com.usc.segmentacion.negocio.modelo.ResultadoCarga;
import com.usc.segmentacion.negocio.servicio.CargaArchivoServicio;
import com.usc.segmentacion.presentacion.dto.RespuestaCarga;
import com.usc.segmentacion.presentacion.dto.RespuestaError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Archivos", description = "Carga y validación del historial de compras")
@RestController
@RequestMapping("/api/v1/archivos")
public class ArchivoController {

    private final CargaArchivoServicio servicio;

    public ArchivoController(CargaArchivoServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Carga un archivo CSV de compras",
            description = "Valida el archivo, guarda las filas correctas y reporta las incidencias de calidad. "
                    + "Columnas obligatorias: id_compra, id_cliente, fecha_compra (AAAA-MM-DD) "
                    + "y valor_compra (mayor que 0, con punto decimal).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Archivo cargado: validado o validado con observaciones"),
            @ApiResponse(responseCode = "400", description = "Archivo ausente, vacío o que no es .csv",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "422", description = "Falta una columna obligatoria o ninguna fila es válida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaCarga cargar(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        String nombre = archivo.getOriginalFilename();
        if (archivo.isEmpty() || nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new FormatoNoAdmitidoException("Se espera un archivo .csv con contenido");
        }
        ResultadoCarga resultado = servicio.cargar(nombre, archivo.getInputStream());
        return RespuestaCarga.desde(resultado);
    }
}