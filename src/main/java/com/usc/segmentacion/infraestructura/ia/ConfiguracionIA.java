package com.usc.segmentacion.infraestructura.ia;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.usc.segmentacion.negocio.contrato.AsistenteDescriptivo;

@Configuration
public class ConfiguracionIA {

    /** El asistente principal.
     * 
     */
    @Bean("asistenteConfigurado")
    public AsistenteDescriptivo asistenteConfigurado(AsistentePlantilla plantilla) {
        return plantilla;
    }
}