package com.usc.segmentacion.presentacion;

import java.io.IOException;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.usc.segmentacion.negocio.modelo.ResultadoCarga;
import com.usc.segmentacion.negocio.servicio.CargaArchivoServicio;
import com.usc.segmentacion.presentacion.dto.RespuestaCarga;

@RestController
@RequestMapping("/api/v1/archivos")
public class ArchivoController {

    private final CargaArchivoServicio servicio;

    public ArchivoController(CargaArchivoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespuestaCarga> cargar(@RequestParam("archivo") MultipartFile archivo)
            throws IOException {
        String nombre = archivo.getOriginalFilename();
        if (archivo.isEmpty() || nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new FormatoNoAdmitidoException("Se espera un archivo .csv con contenido");
        }
        ResultadoCarga resultado = servicio.cargar(nombre, archivo.getInputStream());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaCarga.desde(resultado));
    }
}