package com.github.k1mb1.vkr_backend.group.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.SubgroupsApi;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class SubgroupsControllerTest {

    SubgroupsApi subgroupsApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        subgroupsApi = mock(SubgroupsApi.class);
        client = ControllerTestSupport.client(new SubgroupsController(subgroupsApi));
    }

    // -----------------------------------------------------------------------
    // GET /api/subgroups?groupId=... — happy path
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsReturnsOkWithList() {
        var groupId = UUID.randomUUID();
        var sg1Id = UUID.randomUUID();
        var sg2Id = UUID.randomUUID();

        when(subgroupsApi.getSubgroups(groupId)).thenReturn(List.of(
            SubgroupResponse.builder().id(sg1Id).index(1).build(),
            SubgroupResponse.builder().id(sg2Id).index(2).build()
        ));

        client.get().uri("/api/subgroups?groupId=" + groupId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(sg1Id.toString())
            .jsonPath("$[0].index").isEqualTo(1)
            .jsonPath("$[1].id").isEqualTo(sg2Id.toString())
            .jsonPath("$[1].index").isEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // GET /api/subgroups?groupId=... — group not found → 404
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsGroupNotFoundReturns404() {
        var groupId = UUID.randomUUID();
        when(subgroupsApi.getSubgroups(groupId))
            .thenThrow(new ResourceNotFoundException("Group", groupId));

        client.get().uri("/api/subgroups?groupId=" + groupId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/subgroups?groupId=... — empty list
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsReturnsEmptyListWhenNone() {
        var groupId = UUID.randomUUID();
        when(subgroupsApi.getSubgroups(groupId)).thenReturn(List.of());

        client.get().uri("/api/subgroups?groupId=" + groupId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isEmpty();
    }
}
