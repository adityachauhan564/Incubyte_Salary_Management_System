package com.incubyte.salary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the OpenAPI/Swagger wiring doesn't break the application and
 * actually documents the real endpoints - not a test of springdoc itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentIncludesEveryControllerAndKeyPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Salary Management System API"))
                .andExpect(jsonPath("$.paths./api/employees").exists())
                .andExpect(jsonPath("$.paths./api/employees/{id}").exists())
                .andExpect(jsonPath("$.paths./api/employees/{employeeId}/salaries").exists())
                .andExpect(jsonPath("$.paths./api/employees/{employeeId}/salary/current").exists())
                .andExpect(jsonPath("$.paths./api/employees/{employeeId}/salaries/{salaryId}").exists())
                .andExpect(jsonPath("$.paths./api/analytics/summary").exists())
                .andExpect(jsonPath("$.paths./api/analytics/departments").exists())
                .andExpect(jsonPath("$.paths./api/analytics/countries").exists())
                .andExpect(jsonPath("$.paths./api/analytics/distribution").exists());
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
