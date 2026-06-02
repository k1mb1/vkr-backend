package com.github.k1mb1.vkr_backend;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Интеграционный тест полного стека: HTTP → Security (JWT) → контроллер → сервис →
 * JPA/Hibernate → реальный PostgreSQL в Testcontainers (см. {@link TestcontainersConfiguration}).
 * <p>
 * Аутентификация подменяется {@code jwt()} post-processor'ом Spring Security Test,
 * чтобы не поднимать настоящий IdP, но при этом пройти реальную цепочку фильтров.
 * <p>
 * Требует доступного Docker-демона для запуска контейнера БД.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createsTeacherAndReadsItBack() throws Exception {
        var id = UUID.randomUUID();
        var request = new CreateOrUpdateTeacherRequest("Иванов Иван", "ivanov+" + id + "@example.com");

        mockMvc.perform(put("/api/teachers/{id}", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.username").value("Иванов Иван"));

        // Реально сохранилось в БД и возвращается в выдаче
        mockMvc.perform(get("/api/teachers").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].id").value(hasItem(id.toString())));
    }

    @Test
    void updatesExistingTeacherInPlace() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(put("/api/teachers/{id}", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateOrUpdateTeacherRequest("Первое Имя", "first+" + id + "@example.com"))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/teachers/{id}", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateOrUpdateTeacherRequest("Второе Имя", "second+" + id + "@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.username").value("Второе Имя"));
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/teachers"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        mockMvc.perform(put("/api/teachers/{id}", UUID.randomUUID())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"email\":\"not-an-email\"}"))
            .andExpect(status().isBadRequest());
    }
}
