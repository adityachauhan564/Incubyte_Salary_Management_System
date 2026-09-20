package com.incubyte.salary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression coverage for the GlobalExceptionHandler fix: a genuinely
 * unmapped URL (no controller, no static resource) must return a clean 404
 * in the standard ApiError shape, not the 500 that resulted from
 * NoResourceFoundException falling through to the Exception.class catch-all.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UnmappedRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unmappedUrlReturns404WithTheStandardErrorShapeNotAServerError() throws Exception {
        mockMvc.perform(get("/api/this-route-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No such endpoint."))
                .andExpect(jsonPath("$.path").value("/api/this-route-does-not-exist"))
                .andExpect(jsonPath("$.timestamp").exists())
                // no internal implementation detail (exception class name, package, stack trace) leaks
                .andExpect(jsonPath("$.message", not(containsStringIgnoringCase("Exception"))))
                .andExpect(jsonPath("$.message", not(containsStringIgnoringCase("springframework"))));
    }

    @Test
    void existingValidEndpointStillReturnsItsOwnSuccessfulResponseAfterTheFix() throws Exception {
        mockMvc.perform(get("/api/employees").param("page", "0").param("size", "1"))
                .andExpect(status().isOk());
    }
}
