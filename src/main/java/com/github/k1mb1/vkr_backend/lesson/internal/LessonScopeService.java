package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkReplaceLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonScopeService
    implements LessonScopesApi {

    final LessonRepository lessonRepository;

    final LessonScopeRepository lessonScopeRepository;

    final GroupReferenceService groupReferenceService;

    final LessonMapper lessonMapper;

    @Transactional
    @Override
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<LessonScopeResponse> addScopes(
        UUID lessonId,
        BulkAddLessonScopesRequest request
    ) {
        var lesson = lessonRepository.findWithDetailsById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        var added = new ArrayList<LessonScope>();
        for (var item : request.items()) {
            var scope = LessonScope.builder()
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
    @Override
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<LessonScopeResponse> replaceScopesOfLesson(
        UUID lessonId,
        BulkReplaceLessonScopesRequest request
    ) {
        var ids = request.items().stream().map(BulkReplaceLessonScopesRequest.Item::id).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Duplicate scope ids in request");
        }

        var scopes = lessonScopeRepository.findAllById(ids);
        if (scopes.size() != ids.size()) {
            var found = scopes.stream().map(LessonScope::getId).collect(java.util.stream.Collectors.toSet());
            var missing = ids.stream().filter(id -> !found.contains(id)).toList();
            throw new ResourceNotFoundException("LessonScope", missing.iterator().next());
        }
        for (var s : scopes) {
            if (!s.getLesson().getId().equals(lessonId)) {
                throw new IllegalArgumentException(
                    "LessonScope " + s.getId() + " does not belong to lesson " + lessonId);
            }
        }

        var byId = new HashMap<UUID, LessonScope>();
        for (var s : scopes) {
            byId.put(s.getId(), s);
        }

        for (var item : request.items()) {
            var scope = byId.get(item.id());
            scope.setStartedAt(item.startedAt());
            applyAudience(scope, item.audience());
        }

        var lesson = lessonRepository.findWithDetailsById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        validateNoInternalOverlap(lesson);

        var saved = lessonScopeRepository.saveAll(scopes);
        var savedById = new HashMap<UUID, LessonScope>();
        for (var s : saved) {
            savedById.put(s.getId(), s);
        }
        return ids.stream()
            .map(savedById::get)
            .map(lessonMapper::toScopeResponse)
            .toList();
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canAccessLessonScopes({#scopeId})")
    public void deleteScope(UUID scopeId) {
        var scope = lessonScopeRepository.findById(scopeId)
            .orElseThrow(() -> new ResourceNotFoundException("LessonScope", scopeId));
        scope.archive();
        lessonScopeRepository.save(scope);
    }

    private void applyAudience(LessonScope scope, LessonScopeAudienceRequest audience) {
        if (audience == null) {
            scope.setAllGroups(true);
            scope.setGroup(null);
            scope.setAllowedSubgroup(null);
            return;
        }
        var ref = groupReferenceService.resolveAudience(
            audience.groupId(), audience.allowedSubgroupId());
        scope.setAllGroups(false);
        scope.setGroup(ref.group());
        scope.setAllowedSubgroup(ref.allowedSubgroup());
    }

    private void validateNoInternalOverlap(Lesson lesson) {
        var scopes = new ArrayList<>(lesson.getScopes());
        for (int i = 0; i < scopes.size(); i++) {
            for (int j = i + 1; j < scopes.size(); j++) {
                if (overlaps(scopes.get(i), scopes.get(j))) {
                    throw new IllegalArgumentException(
                        "Scope audience overlap within lesson " + lesson.getId() + " between " + describe(
                            scopes.get(i)) + " and " + describe(scopes.get(j)));
                }
            }
        }
    }

    private String describe(LessonScope s) {
        if (s.getId() != null) {
            return s.getId().toString();
        }
        if (s.isAllGroups()) {
            return "[new allGroups]";
        }
        return "[new group=" + (s.getGroup() != null
                                ? s.getGroup().getId()
                                : null) + ", subgroup=" + (s.getAllowedSubgroup() != null
                                                           ? s.getAllowedSubgroup().getId()
                                                           : null) + "]";
    }

    private boolean overlaps(LessonScope a, LessonScope b) {
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
