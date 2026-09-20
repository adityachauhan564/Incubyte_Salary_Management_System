package com.incubyte.salary.employee.controller;

import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.employee.dto.EmployeeResponse;
import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getByIdReturnsTheEmployeeWhenFound() throws Exception {
        when(employeeService.getById(1L)).thenReturn(
                new EmployeeResponse(1L, "Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer"));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastName").value("Lovelace"));
    }

    @Test
    void getByIdReturns404WhenTheEmployeeDoesNotExist() throws Exception {
        when(employeeService.getById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/employees/99"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchAppliesFiltersAndPaginationAndReturnsAPagedBody() throws Exception {
        EmployeeResponse employee = new EmployeeResponse(
                2L, "Grace", "Hopper", "United States", "Engineering", "Rear Admiral");
        Pageable expectedPageable = PageRequest.of(1, 5);
        Page<EmployeeResponse> page = new PageImpl<>(List.of(employee), expectedPageable, 6);
        when(employeeService.search(any(EmployeeSearchCriteria.class), eq(expectedPageable))).thenReturn(page);

        mockMvc.perform(get("/api/employees")
                        .param("country", "United States")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Hopper"))
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(1));

        var criteriaCaptor = org.mockito.ArgumentCaptor.forClass(EmployeeSearchCriteria.class);
        verify(employeeService).search(criteriaCaptor.capture(), eq(expectedPageable));
        assertThat(criteriaCaptor.getValue().country()).isEqualTo("United States");
    }
}
