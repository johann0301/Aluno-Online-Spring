package br.com.alunoonline.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Aluno Online API",
                version = "1.0.0",
                description = "API academica para gerenciar alunos, professores, disciplinas, matriculas, boletim e relatorios.",
                contact = @Contact(name = "Aluno Online"),
                license = @License(name = "Uso academico")
        ),
        servers = {
                @Server(url = "/", description = "Servidor da aplicação")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().components(new Components()
                .addSecuritySchemes("bearer-key", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
