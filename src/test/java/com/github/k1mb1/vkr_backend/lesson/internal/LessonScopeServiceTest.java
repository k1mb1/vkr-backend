package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkReplaceLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import java.time.LocalDate;
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

    @Mock LessonRepository lessonRepository;
    @Mock LessonScopeRepository lessonScopeRepository;
    @Mock GroupReferenceService groupReferenceService;
    @Mock LessonMapper lessonMapper;

    @InjectMocks LessonScopeService service;

    // -------------------------------------------------------------------------
    // addScopes — lesson not found
    // -------------------------------------------------------------------------

    @Test
    void addScopes_lessonNotFound_throwsResourceNotFoundException() {
        var lessonId = UUID.randomUUID();
        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.empty());

        var request = new BulkAddLessonScopesRequest(
            List.of(new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 1, 10)))
        );

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Lesson");
    }

    // -------------------------------------------------------------------------
    // addScopes — overlap: two allGroups scopes
    // -------------------------------------------------------------------------

    @Test
    void addScopes_twoAllGroupsScopes_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("Math").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));

        // Two items with null audience => allGroups=true for both => overlap
        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 1, 10)),
            new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 1, 17))
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }

    // -------------------------------------------------------------------------
    // addScopes — allGroups conflicts with existing group scope already in lesson
    // -------------------------------------------------------------------------

    @Test
    void addScopes_newAllGroupsOverlapsExistingGroupScope_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("Physics").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var group = Group.builder().id(UUID.randomUUID()).name("Group A").build();
        var existingScope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(group)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 2, 1))
            .build();
        lesson.getScopes().add(existingScope);

        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));

        // Adding a new allGroups scope — overlaps with the existing group-specific scope
        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 2, 8))
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }

    // -------------------------------------------------------------------------
    // addScopes — same group, same subgroup → overlap
    // -------------------------------------------------------------------------

    @Test
    void addScopes_sameGroupAndSameSubgroup_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("Chemistry").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .build();

        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group B").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(group).build();

        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));
        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(group);
        when(groupReferenceService.getSubgroupReferenceById(subgroupId)).thenReturn(subgroup);

        var audience = new LessonScopeAudienceRequest(groupId, subgroupId);
        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(audience, LocalDate.of(2025, 3, 1)),
            new BulkAddLessonScopesRequest.Item(audience, LocalDate.of(2025, 3, 8))
        ));

        assertThatThrownBy(() -> service.addScopes(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }

    // -------------------------------------------------------------------------
    // addScopes — different groups, no overlap → success
    // -------------------------------------------------------------------------

    @Test
    void addScopes_differentGroups_noOverlap_returnsSavedScopes() {
        var lessonId = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("History").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var groupId1 = UUID.randomUUID();
        var groupId2 = UUID.randomUUID();
        var group1 = Group.builder().id(groupId1).name("Group X").build();
        var group2 = Group.builder().id(groupId2).name("Group Y").build();

        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));
        when(groupReferenceService.getGroupReferenceById(groupId1)).thenReturn(group1);
        when(groupReferenceService.getGroupReferenceById(groupId2)).thenReturn(group2);

        var savedScopeResponse = new LessonScopeResponse(
            UUID.randomUUID(), groupId1, "Group X", null, null, LocalDate.of(2025, 4, 1), false
        );
        when(lessonScopeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonMapper.toScopeResponse(any(LessonScope.class))).thenReturn(savedScopeResponse);

        var request = new BulkAddLessonScopesRequest(List.of(
            new BulkAddLessonScopesRequest.Item(
                new LessonScopeAudienceRequest(groupId1, null), LocalDate.of(2025, 4, 1)
            ),
            new BulkAddLessonScopesRequest.Item(
                new LessonScopeAudienceRequest(groupId2, null), LocalDate.of(2025, 4, 1)
            )
        ));

        var result = service.addScopes(lessonId, request);

        assertThat(result).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // replaceScopesOfLesson — scope not found (missing id)
    // -------------------------------------------------------------------------

    @Test
    void replaceScopesOfLesson_scopeNotFound_throwsResourceNotFoundException() {
        var lessonId = UUID.randomUUID();
        var missingId = UUID.randomUUID();

        when(lessonScopeRepository.findAllById(List.of(missingId))).thenReturn(List.of());

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(missingId, null, LocalDate.of(2025, 5, 1))
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("LessonScope");
    }

    // -------------------------------------------------------------------------
    // replaceScopesOfLesson — scope belongs to different lesson
    // -------------------------------------------------------------------------

    @Test
    void replaceScopesOfLesson_scopeBelongsToDifferentLesson_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var otherLessonId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        var subject = Subject.builder().id(UUID.randomUUID()).name("Biology").build();
        var otherLesson = Lesson.builder()
            .id(otherLessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(scopeId)
            .lesson(otherLesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 6, 1))
            .build();

        when(lessonScopeRepository.findAllById(List.of(scopeId))).thenReturn(List.of(scope));

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, LocalDate.of(2025, 6, 1))
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to lesson");
    }

    // -------------------------------------------------------------------------
    // replaceScopesOfLesson — duplicate ids in request
    // -------------------------------------------------------------------------

    @Test
    void replaceScopesOfLesson_duplicateScopeIds_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, LocalDate.of(2025, 7, 1)),
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, LocalDate.of(2025, 7, 8))
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate scope ids");
    }

    // -------------------------------------------------------------------------
    // replaceScopesOfLesson — happy path: single allGroups scope updates ok
    // -------------------------------------------------------------------------

    @Test
    void replaceScopesOfLesson_validRequest_returnsUpdatedScopes() {
        var lessonId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var newDate = LocalDate.of(2025, 8, 15);

        var subject = Subject.builder().id(UUID.randomUUID()).name("Art").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(scopeId)
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 8, 1))
            .build();
        lesson.getScopes().add(scope);

        when(lessonScopeRepository.findAllById(List.of(scopeId))).thenReturn(List.of(scope));
        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonScopeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        var expectedResponse = new LessonScopeResponse(
            scopeId, null, null, null, null, newDate, true
        );
        when(lessonMapper.toScopeResponse(scope)).thenReturn(expectedResponse);

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId, null, newDate)
        ));

        var result = service.replaceScopesOfLesson(lessonId, request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).startedAt()).isEqualTo(newDate);
    }

    // -------------------------------------------------------------------------
    // replaceScopesOfLesson — overlap after update throws
    // -------------------------------------------------------------------------

    @Test
    void replaceScopesOfLesson_postUpdateOverlapBetweenExistingScopes_throwsIllegalArgumentException() {
        var lessonId = UUID.randomUUID();
        var scopeId1 = UUID.randomUUID();
        var scopeId2 = UUID.randomUUID();

        var subject = Subject.builder().id(UUID.randomUUID()).name("Music").build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        // Two allGroups scopes already in the lesson — they will overlap after update
        var scope1 = LessonScope.builder()
            .id(scopeId1)
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 9, 1))
            .build();
        var scope2 = LessonScope.builder()
            .id(scopeId2)
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 9, 8))
            .build();
        lesson.getScopes().add(scope1);
        lesson.getScopes().add(scope2);

        when(lessonScopeRepository.findAllById(List.of(scopeId1))).thenReturn(List.of(scope1));
        when(lessonRepository.findWithDetailsById(lessonId)).thenReturn(Optional.of(lesson));

        var request = new BulkReplaceLessonScopesRequest(List.of(
            new BulkReplaceLessonScopesRequest.Item(scopeId1, null, LocalDate.of(2025, 9, 15))
        ));

        assertThatThrownBy(() -> service.replaceScopesOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }
}
