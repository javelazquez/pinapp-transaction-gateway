package com.pinapp.gateway.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pinapp Transaction Gateway API")
                        .description("API para el procesamiento de transacciones y notificaciones agnósticas.")
                        .version("1.0.0"));
    }
}
