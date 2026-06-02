package com.github.k1mb1.vkr_backend.teacher.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import com.github.k1mb1.vkr_backend.teacher.TeachersApi;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Контроллерные тесты на RestTestClient.bindToController — проверяют маршрутизацию,
 * сериализацию и делегирование в API-слой без поднятия Spring-контекста и БД.
 */
class TeachersControllerTest {

    TeachersApi teachersApi;

    RestTestClient client;

    @BeforeEach
    void setUp() {
        teachersApi = mock(TeachersApi.class);
        client = ControllerTestSupport.client(new TeachersController(teachersApi));
    }

    @Test
    void createOrUpdateReturnsTeacher() {
        var id = UUID.randomUUID();
        when(teachersApi.createOrUpdateTeacher(eq(id), any()))
            .thenReturn(TeacherResponse.builder().id(id).username("ivanov").email("ivanov@example.com").build());

        client.put().uri("/api/teachers/{id}", id)
            .body(new CreateOrUpdateTeacherRequest("ivanov", "ivanov@example.com"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.username").isEqualTo("ivanov")
            .jsonPath("$.email").isEqualTo("ivanov@example.com");
    }

    @Test
    void getPageReturnsTeachers() {
        when(teachersApi.getPage(any(), any()))
            .thenReturn(new PageImpl<>(List.of(
                TeacherResponse.builder().id(UUID.randomUUID()).username("petrov").email("p@example.com").build()
            ), PageRequest.of(0, 20), 1));

        client.get().uri("/api/teachers")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content[0].username").isEqualTo("petrov");
    }
}
