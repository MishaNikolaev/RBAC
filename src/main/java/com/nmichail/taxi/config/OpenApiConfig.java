package com.nmichail.taxi.config;

import com.nmichail.taxi.controller.AuthController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI taxiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taxi / RBAC API")
                        .version("1.0.0")
                        .description("""
                                REST API: поездки, водители, пассажиры, уведомления, JWT-аутентификация.
                                
                                **Авторизация:** сначала `POST /auth/login`, затем **Authorize** в Swagger/Scalar — вставьте JWT, или заголовок `Authorization: Bearer <token>`.
                                
                                **UI:** Swagger [/swagger-ui/index.html](/swagger-ui/index.html) · Scalar [/scalar](/scalar).
                                """)
                        .contact(new Contact().name("API").email("dev@local")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT, выдаётся `POST /auth/login`")));
    }

    @Bean
    OperationCustomizer bearerAuthOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.getMethod().getDeclaringClass().equals(AuthController.class)) {
                return operation;
            }
            operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
            return operation;
        };
    }
}
