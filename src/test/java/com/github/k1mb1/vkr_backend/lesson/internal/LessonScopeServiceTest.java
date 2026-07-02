package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.AudienceRef;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkReplaceLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonScopeServiceTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LessonScopeRepository lessonScopeRepository;

    @Mock
    GroupReferenceService groupReferenceService;

    @Mock
    LessonMapper lessonMapper;

    @InjectMocks
    LessonScopeService service;

    final UUID lessonId = UUID.randomUUID();

    private Lesson lessonWithNoScopes() {
        return Lesson.builder().id(lessonId).scopes(new HashSet<>()).build();
    }

    private LessonScopeAudienceRequest groupAudience(UUID groupId) {
        return new LessonScopeAudienceRequest(groupId, null);
    }

    @Test
    void addScopesRejectsOverlapBetweenAllGroupsAndGroup() {
        when(lessonRepository.findWithDetailsById(lessonId))
            .thenReturn(Optional.of(lessonWithNoScopes()));
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        lenient().when(groupReferenceService.resolveAudience(groupId, null))
            .thenReturn(new AudienceRef(group, null));

        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(null, LocalDate.now()),          // allGroups
            new BulkAddLessonScopesRequest.Item(groupAudience(groupId), LocalDate.now())
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }

    @Test
    void addScopesPersistsDistinctGroups() {
        when(lessonRepository.findWithDetailsById(lessonId))
            .thenReturn(Optional.of(lessonWithNoScopes()));
        var g1 = UUID.randomUUID();
        var g2 = UUID.randomUUID();
        when(groupReferenceService.resolveAudience(g1, null))
            .thenReturn(new AudienceRef(Group.builder().id(g1).name("G1").build(), null));
        when(groupReferenceService.resolveAudience(g2, null))
            .thenReturn(new AudienceRef(Group.builder().id(g2).name("G2").build(), null));
        when(lessonScopeRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonMapper.toScopeResponse(any())).thenReturn(mock(LessonScopeResponse.class));

        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(groupAudience(g1), LocalDate.now()),
            new BulkAddLessonScopesRequest.Item(groupAudience(g2), LocalDate.now())
        ));

        assertThat(service.addScopes(lessonId, request)).hasSize(2);
    }

    @Test
    void addScopesRejectsSubgroupFromAnotherGroup() {
        when(lessonRepository.findWithDetailsById(lessonId))
            .thenReturn(Optional.of(lessonWithNoScopes()));
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        when(groupReferenceService.resolveAudience(groupId, subgroupId))
            .thenThrow(new IllegalArgumentException("Subgroup does not belong to the specified group"));

        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(
                new LessonScopeAudienceRequest(groupId, subgroupId), LocalDate.now())
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Subgroup does not belong");
    }

    @Test
    void addScopesThrowsWhenLessonMissing() {
        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.empty());

        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(null, LocalDate.now())
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void replaceRejectsDuplicateIds() {
        var dupId = UUID.randomUUID();
        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(dupId, null, LocalDate.now()),
            new BulkReplaceLessonScopesRequest.Item(dupId, null, LocalDate.now())
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate scope ids");
    }

    @Test
    void replaceRejectsScopeFromAnotherLesson() {
        var scopeId = UUID.randomUUID();
        var foreignLesson = Lesson.builder().id(UUID.randomUUID()).build();
        var scope = LessonScope.builder().id(scopeId).lesson(foreignLesson).build();
        when(lessonScopeRepository.findAllById(List.of(scopeId))).thenReturn(List.of(scope));

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, LocalDate.now())
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to lesson");
    }

    @Test
    void replaceThrowsWhenScopeMissing() {
        var scopeId = UUID.randomUUID();
        when(lessonScopeRepository.findAllById(List.of(scopeId))).thenReturn(List.of());

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, LocalDate.now())
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
