package com.ecutrans9000.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "EcuTrans9000 API",
        version = "v1",
        description = "Documentacion OpenAPI para autenticacion, usuarios, dashboard y modulo de vehiculos (CRUD, estados, soft delete, imagenes y CSV).",
        contact = @Contact(name = "EcuTrans9000", email = "soporte@ecutrans9000.local"),
        license = @License(name = "Proprietary")
    ),
    security = {
        @SecurityRequirement(name = "bearerAuth")
    },
    servers = {
        @Server(url = "http://localhost:8080", description = "Local")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
/**
 * Componente publico de backend para OpenApiConfig.
 */
public class OpenApiConfig {
}
