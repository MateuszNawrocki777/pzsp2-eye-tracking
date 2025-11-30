package org.pzsp2.eye_tracking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eyeTrackingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eye Tracking API")
                        .description("Endpoints for authentication and eye-tracking research workflows")
                        .version("0.0.1")
                        .contact(new Contact().name("PZSP2 Team")));
    }
}
