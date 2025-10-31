package com.aurionpro.payroll.config;


import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;


//This class defines how your API is documented using OpenAPI 3.
//It provides metadata like title, version, description, and contact details.
//It defines where the API can be accessed (e.g., a local server at http://localhost:8080).
//Spring manages this configuration via @Configuration and @Bean.

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI baseOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Organization Payment App")
            .version("v1.0.0")
            .description("Payment and Payroll Management System with JWT security using bearer tokens and frontend using Angular")
            .contact(new Contact().name("Organization Payment App").email("srushtipatane08@gmail.com")))
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Local")
        ));
  }
}
