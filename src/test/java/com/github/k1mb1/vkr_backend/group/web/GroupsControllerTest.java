package com.github.k1mb1.vkr_backend.group.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupWithSubgroupsResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

class GroupsControllerTest {

    GroupsApi groupsApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        groupsApi = mock(GroupsApi.class);
        client = ControllerTestSupport.client(new GroupsController(groupsApi));
    }

    // -----------------------------------------------------------------------
    // GET /api/groups — page
    // -----------------------------------------------------------------------

    @Test
    void getGroupPageReturnsOkWithPageContent() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var item = GroupPageResponse.builder().id(id).name("ПМИ-101").createdAt(now).updatedAt(now).build();
        when(groupsApi.getGroupPage(any(), any()))
            .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        client.get().uri("/api/groups")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content[0].id").isEqualTo(id.toString())
            .jsonPath("$.content[0].name").isEqualTo("ПМИ-101");
    }

    // -----------------------------------------------------------------------
    // POST /api/groups — create 201
    // -----------------------------------------------------------------------

    @Test
    void createGroupReturns201WithBody() {
        var id = UUID.randomUUID();
        var response = GroupResponse.builder()
            .id(id).name("ПМИ-101").subgroups(List.of()).students(List.of()).build();
        when(groupsApi.createGroup(any())).thenReturn(response);

        var requestBody = CreateGroupRequest.builder()
            .name("ПМИ-101")
            .students(List.of(StudentGroupMemberRequest.builder().username("alice").build()))
            .build();

        client.post().uri("/api/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.name").isEqualTo("ПМИ-101");
    }

    // -----------------------------------------------------------------------
    // PATCH /api/groups/{id} — update 200
    // -----------------------------------------------------------------------

    @Test
    void updateGroupReturnsOk() {
        var id = UUID.randomUUID();
        var response = GroupResponse.builder()
            .id(id).name("NewName").subgroups(List.of()).students(List.of()).build();
        when(groupsApi.updateGroup(eq(id), any())).thenReturn(response);

        var requestBody = UpdateGroupRequest.builder().name("NewName").build();

        client.patch().uri("/api/groups/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("NewName");
    }

    @Test
    void updateGroupNotFoundReturns404() {
        var id = UUID.randomUUID();
        when(groupsApi.updateGroup(eq(id), any()))
            .thenThrow(new EntityNotFoundException("Group not found: " + id));

        var requestBody = UpdateGroupRequest.builder().name("X").build();

        client.patch().uri("/api/groups/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/groups/{id}
    // -----------------------------------------------------------------------

    @Test
    void getGroupByIdReturnsOk() {
        var id = UUID.randomUUID();
        var response = GroupResponse.builder()
            .id(id).name("ПМИ").subgroups(List.of()).students(List.of()).build();
        when(groupsApi.getGroupById(id)).thenReturn(response);

        client.get().uri("/api/groups/{id}", id)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.name").isEqualTo("ПМИ");
    }

    @Test
    void getGroupByIdNotFoundReturns404() {
        var id = UUID.randomUUID();
        when(groupsApi.getGroupById(id))
            .thenThrow(new EntityNotFoundException("Group not found: " + id));

        client.get().uri("/api/groups/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // DELETE /api/groups/{id} — 204
    // -----------------------------------------------------------------------

    @Test
    void deleteGroupReturns204() {
        var id = UUID.randomUUID();
        doNothing().when(groupsApi).deleteGroup(id);

        client.delete().uri("/api/groups/{id}", id)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void deleteGroupNotFoundReturns404() {
        var id = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Group not found: " + id))
            .when(groupsApi).deleteGroup(id);

        client.delete().uri("/api/groups/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // POST /api/groups/{groupId}/subjects/{subjectId} — attach
    // -----------------------------------------------------------------------

    @Test
    void attachToSubjectReturnsOk() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var response = GroupResponse.builder()
            .id(groupId).name("G").subgroups(List.of()).students(List.of()).build();
        when(groupsApi.attachToSubject(groupId, subjectId)).thenReturn(response);

        client.post().uri("/api/groups/{groupId}/subjects/{subjectId}", groupId, subjectId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(groupId.toString());
    }

    @Test
    void attachToSubjectDuplicateReturns400() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupsApi.attachToSubject(groupId, subjectId))
            .thenThrow(new IllegalStateException("already attached"));

        client.post().uri("/api/groups/{groupId}/subjects/{subjectId}", groupId, subjectId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void attachToSubjectGroupNotFoundReturns404() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupsApi.attachToSubject(groupId, subjectId))
            .thenThrow(new EntityNotFoundException("Group not found: " + groupId));

        client.post().uri("/api/groups/{groupId}/subjects/{subjectId}", groupId, subjectId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // DELETE /api/groups/{groupId}/subjects/{subjectId} — detach 204
    // -----------------------------------------------------------------------

    @Test
    void detachFromSubjectReturns204() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        doNothing().when(groupsApi).detachFromSubject(groupId, subjectId);

        client.delete().uri("/api/groups/{groupId}/subjects/{subjectId}", groupId, subjectId)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void detachFromSubjectNotAttachedReturns404() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("not attached"))
            .when(groupsApi).detachFromSubject(groupId, subjectId);

        client.delete().uri("/api/groups/{groupId}/subjects/{subjectId}", groupId, subjectId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/groups/by-subject?subjectId=...
    // -----------------------------------------------------------------------

    @Test
    void getGroupsBySubjectReturnsOkWithList() {
        var subjectId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var response = GroupWithSubgroupsResponse.builder()
            .id(groupId).name("G").subgroups(List.of()).build();
        when(groupsApi.getGroupsBySubjectId(subjectId)).thenReturn(List.of(response));

        client.get().uri("/api/groups/by-subject?subjectId=" + subjectId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(groupId.toString());
    }
}
