package com.github.k1mb1.vkr_backend.lesson.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.mapper.LessonMapper;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkReplaceLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.response.LessonScopeResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonScopeService {

    final LessonRepository lessonRepository;

    final LessonScopeRepository lessonScopeRepository;

    final GroupReferenceService groupReferenceService;

    final LessonMapper lessonMapper;

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<LessonScopeResponse> addScopes(UUID lessonId, BulkAddLessonScopesRequest request) {
        var lesson = lessonRepository
                .findWithDetailsById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        var added = new ArrayList<LessonScopeEntity>();
        for (var item : request.items()) {
            var scope = LessonScopeEntity.builder()
                    .lesson(lesson)
                    .startedAt(item.startedAt())
                    .build();
            applyAudience(scope, item.audience());
            lesson.getScopes().add(scope);
            added.add(scope);
        }

        validateNoInternalOverlap(lesson);

        var saved = lessonScopeRepository.saveAll(added);
        return saved.stream().map(lessonMapper::toScopeResponse).toList();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<LessonScopeResponse> replaceScopesOfLesson(UUID lessonId, BulkReplaceLessonScopesRequest request) {
        var ids = request.items().stream()
                .map(BulkReplaceLessonScopesRequest.Item::id)
                .toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Duplicate scope ids in request");
        }

        var scopes = lessonScopeRepository.findAllById(ids);
        if (scopes.size() != ids.size()) {
            var found = scopes.stream().map(LessonScopeEntity::getId).collect(java.util.stream.Collectors.toSet());
            var missing = ids.stream().filter(id -> !found.contains(id)).toList();
            throw new ResourceNotFoundException(
                    "LessonScope", missing.iterator().next());
        }
        for (var s : scopes) {
            if (!s.getLesson().getId().equals(lessonId)) {
                throw new IllegalArgumentException(
                        "LessonScope " + s.getId() + " does not belong to lesson " + lessonId);
            }
        }

        var byId = new HashMap<UUID, LessonScopeEntity>();
        for (var s : scopes) {
            byId.put(s.getId(), s);
        }

        for (var item : request.items()) {
            var scope = Objects.requireNonNull(byId.get(item.id()));
            scope.setStartedAt(item.startedAt());
            applyAudience(scope, item.audience());
        }

        var lesson = lessonRepository
                .findWithDetailsById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        validateNoInternalOverlap(lesson);

        var saved = lessonScopeRepository.saveAll(scopes);
        var savedById = new HashMap<UUID, LessonScopeEntity>();
        for (var s : saved) {
            savedById.put(s.getId(), s);
        }
        return ids.stream()
                .map(savedById::get)
                .map(lessonMapper::toScopeResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLessonScopes({#scopeId})")
    public void deleteScope(UUID scopeId) {
        var scope = lessonScopeRepository
                .findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonScope", scopeId));
        scope.archive();
        lessonScopeRepository.save(scope);
    }

    private void applyAudience(LessonScopeEntity scope, @Nullable LessonScopeAudienceRequest audience) {
        if (audience == null) {
            scope.setAllGroups(true);
            scope.setGroup(null);
            scope.setAllowedSubgroup(null);
            return;
        }
        var ref = groupReferenceService.resolveAudience(audience.groupId(), audience.allowedSubgroupId());
        scope.setAllGroups(false);
        scope.setGroup(ref.group());
        scope.setAllowedSubgroup(ref.allowedSubgroup());
    }

    private void validateNoInternalOverlap(LessonEntity lesson) {
        var scopes = new ArrayList<>(lesson.getScopes());
        for (int i = 0; i < scopes.size(); i++) {
            for (int j = i + 1; j < scopes.size(); j++) {
                if (overlaps(scopes.get(i), scopes.get(j))) {
                    throw new IllegalArgumentException("Scope audience overlap within lesson " + lesson.getId()
                            + " between " + describe(scopes.get(i)) + " and " + describe(scopes.get(j)));
                }
            }
        }
    }

    private String describe(LessonScopeEntity s) {
        if (s.getId() != null) {
            return s.getId().toString();
        }
        if (s.isAllGroups()) {
            return "[new allGroups]";
        }
        return "[new group=" + (s.getGroup() != null ? s.getGroup().getId() : null) + ", subgroup="
                + (s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getId() : null) + "]";
    }

    private boolean overlaps(LessonScopeEntity a, LessonScopeEntity b) {
        if (a.isAllGroups() || b.isAllGroups()) {
            return true;
        }
        var ga = a.getGroup();
        var gb = b.getGroup();
        if (ga == null || gb == null || !ga.getId().equals(gb.getId())) {
            return false;
        }
        var sa = a.getAllowedSubgroup();
        var sb = b.getAllowedSubgroup();
        if (sa == null || sb == null) {
            return true;
        }
        return sa.getId().equals(sb.getId());
    }
}
