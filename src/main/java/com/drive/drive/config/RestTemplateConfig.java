package com.drive.drive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RestTemplateConfig  {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    // Configuración de RestTemplate
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();


        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // Aquí puedes logear el cuerpo de la respuesta para ver el mensaje de error exacto
                try {
                    log.error("Response error: {} {}", response.getStatusCode(), response.getStatusText());
                    super.handleError(response);
                } catch (RestClientException e) {
                    log.error("Error handling response: " + e.getMessage());
                    // Aquí puedes lanzar una excepción personalizada o manejar el error como lo consideres apropiado
                }
            }
        });

        return restTemplate;
    }
}
