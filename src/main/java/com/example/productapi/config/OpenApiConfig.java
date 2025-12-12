package com.example.productapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product API")
                        .version("1.0.0")
                        .description("API para gestión de productos con PostgreSQL")
                        .contact(new Contact()
                                .name("Martin Desarrollo")
                                .email("martin@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
