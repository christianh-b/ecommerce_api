package com.cbordon.articulos.proyecto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce REST API")
                        .description("""
                                API RESTful para gestión de artículos de e-commerce con autenticación JWT.

                                **Endpoints públicos:** GET /api/articulos y /api/auth no requieren token.

                                **Endpoints protegidos (ROLE_ADMIN):** POST, PUT y DELETE de artículos.
                                Para autenticarte: registrate o iniciá sesión, copiá el token y pegalo
                                en el botón **Authorize** como `Bearer <token>`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Christian Bordon")
                                .url("https://github.com/christianbordon")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .name("Bearer Auth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
