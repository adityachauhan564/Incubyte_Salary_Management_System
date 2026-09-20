package com.incubyte.salary.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Top-level metadata for the generated OpenAPI document. No security scheme
 * is declared here: this API has no authentication (out of scope for the
 * assessment - see docs/assumptions.md #1), so there is nothing to document.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salaryManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Salary Management System API")
                .version("v1")
                .description("""
                        REST API for an HR Manager to search employees, manage their salary \
                        history, and analyze compensation across departments and countries. \
                        Single-user scope: no authentication (see docs/assumptions.md #1).

                        Money is normalized to USD for cross-currency comparisons. Salary \
                        records are append-only history: a POST creates a new point in an \
                        employee's history (e.g. a raise); a PUT corrects an existing record's \
                        fields in place. The current salary is always derived from that \
                        history, never stored."""));
    }
}
