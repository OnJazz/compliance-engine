package com.jasonvennin.compliance.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI complianceEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Compliance Engine API")
                        .version("1.0.0")
                        .description(
                                "REST API for evaluating financial transactions "
                                        + "against compliance rules."
                        ));
    }
}