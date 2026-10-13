package com.nttdata.ms_reservas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Cliente HTTP apuntando al catálogo. */
@Configuration
public class ClienteConfig {

    // TODO 8 [COMPLETADO]: crea un @Bean RestClient con baseUrl leída de la propiedad vehiculos.url
    //         mediante @Value("${vehiculos.url}").
    @Bean
    RestClient vehiculosClient(@Value("${vehiculos.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .build();
    }
}
