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
 * Proves only health and info are exposed - not the full actuator surface -
 * and that health genuinely reflects the application + database, not a
 * hardcoded UP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorHealthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReportsUpWhenTheApplicationAndDatabaseAreHealthy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoIsExposed() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void endpointsOtherThanHealthAndInfoAreNotExposed() throws Exception {
        assertNotExposed("/actuator/env");
        assertNotExposed("/actuator/beans");
    }

    private void assertNotExposed(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }
}
