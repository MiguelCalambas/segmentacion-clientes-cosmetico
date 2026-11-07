package com.usc.segmentacion.presentacion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {

    @Bean
    public OpenAPI apiSegmentacion() {
        return new OpenAPI().info(new Info()
                .title("API de segmentación de clientes del sector cosmético")
                .version("v1")
                .description("Carga un historial de compras en CSV, lo valida y segmenta a los clientes "
                        + "con K-means para apoyar decisiones comerciales."));
    }
}