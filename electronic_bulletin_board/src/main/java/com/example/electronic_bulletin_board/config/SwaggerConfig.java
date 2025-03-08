package com.example.electronic_bulletin_board.config;

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
                        .title("Electronic Bulletin Board API")
                        .version("1.0")
                        .description("API for managing users and authentication in the Electronic Bulletin Board system"));
    }
}