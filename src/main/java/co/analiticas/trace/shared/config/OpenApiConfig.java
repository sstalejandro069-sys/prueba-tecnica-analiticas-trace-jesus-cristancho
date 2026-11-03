package co.analiticas.trace.shared.config;

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

    public static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI analiticasTraceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analíticas Trace API")
                        .version("1.0.0")
                        .description("""
                                API del prototipo Analíticas Trace: autenticación simulada, ingesta de reportes
                                de inspección (.csv / .xlsx) y analítica descriptiva.

                                **Flujo de prueba:** 1) `POST /api/auth/login` con admin / admin123,
                                2) botón *Authorize* con el token, 3) `POST /api/inspections/upload`,
                                4) `GET /api/analytics`.""")
                        .contact(new Contact().name("Analíticas S.A.S").email("contacto@analiticassas.com")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("Token obtenido en /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
