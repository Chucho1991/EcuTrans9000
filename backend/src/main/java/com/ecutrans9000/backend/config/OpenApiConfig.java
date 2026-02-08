package com.ecutrans9000.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "EcuTrans9000 API",
        version = "v1",
        description = "Documentacion OpenAPI para los servicios de EcuTrans9000.",
        contact = @Contact(name = "EcuTrans9000", email = "soporte@ecutrans9000.local"),
        license = @License(name = "Proprietary")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local")
    }
)
public class OpenApiConfig {
}
