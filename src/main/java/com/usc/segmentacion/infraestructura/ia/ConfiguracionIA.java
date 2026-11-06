package com.usc.segmentacion.infraestructura.ia;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;

@Configuration
public class ConfiguracionIA {

    private static final Logger registro = LoggerFactory.getLogger(ConfiguracionIA.class);

    /** Elige el asistente principal según PROVEEDOR_IA. Sin configuración válida, usa la plantilla. */
    @Bean("asistenteConfigurado")
    public AsistenteDescriptivo asistenteConfigurado(
            @Value("${ia.proveedor:plantilla}") String proveedor,
            @Value("${ia.gemini.url}") String url,
            @Value("${ia.gemini.clave:}") String clave,
            @Value("${ia.gemini.modelo:}") String modelo,
            @Value("${ia.gemini.timeout-segundos:15}") int timeoutSegundos,
            AsistentePlantilla plantilla) {

        if (ClienteIAGemini.NOMBRE.equalsIgnoreCase(proveedor.trim())) {
            if (clave.isBlank() || modelo.isBlank()) {
                registro.warn("PROVEEDOR_IA=gemini pero faltan GEMINI_API_KEY o GEMINI_MODEL: se usará la plantilla");
                return plantilla;
            }
            registro.info("Proveedor de IA configurado: gemini (modelo {})", modelo);
            return new ClienteIAGemini(url, clave, modelo, Duration.ofSeconds(timeoutSegundos), Duration.ofSeconds(1));
        }
        registro.info("Proveedor de IA configurado: plantilla (sin IA externa)");
        return plantilla;
    }
}