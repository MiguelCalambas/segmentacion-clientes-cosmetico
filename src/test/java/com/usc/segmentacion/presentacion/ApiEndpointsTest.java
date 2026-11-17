package com.usc.segmentacion.presentacion;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ia.proveedor=plantilla")
@AutoConfigureMockMvc
@Transactional
class ApiEndpointsTest {

    @Autowired
    private MockMvc mvc;

    private static MockMultipartFile archivo(String nombre, String contenido) {
        return new MockMultipartFile("archivo", nombre, "text/csv",
                contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void cargarCsvValidoDevuelve201() throws Exception {
        String csv = "id_compra,id_cliente,fecha_compra,valor_compra\n"
                + "T-1,C-1,2026-01-10,85000\nT-2,C-1,2026-02-14,60000\n";
        mvc.perform(multipart("/api/v1/archivos").file(archivo("compras.csv", csv)))
                .andExpect(status().isCreated());
    }

    @Test
    void cargarArchivoQueNoEsCsvDevuelve400() throws Exception {
        mvc.perform(multipart("/api/v1/archivos").file(archivo("compras.txt", "hola")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("formato_no_admitido"));
    }

    @Test
    void cargarCsvSinColumnaObligatoriaDevuelve422() throws Exception {
        String csv = "id_compra,id_cliente,valor_compra\n3001,C-020,50000\n";
        mvc.perform(multipart("/api/v1/archivos").file(archivo("sin_fecha.csv", csv)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void consultarEjecucionInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/ejecuciones/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("no_encontrado"));
    }

    @Test
    void consultarEjecucionConIdNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/v1/ejecuciones/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("parametro_de_ruta_invalido"));
    }

    @Test
    void consultarSegmentosDeEjecucionInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/ejecuciones/999999/segmentos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void metodoNoPermitidoDevuelve405EnLugarDeError500() throws Exception {
        mvc.perform(get("/api/v1/archivos"))
                .andExpect(status().isMethodNotAllowed());
    }
}