package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.security.SecurityService;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectService
    implements SubjectsApi {

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final SubjectMapper subjectMapper;

    final TeacherReferenceService teacherReferenceService;

    final GroupReferenceService groupReferenceService;

    final SecurityService securityService;

    @Transactional
    @Override
    @PreAuthorize("@authz.canManageSubject(#id)")
    public SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request) {
        var subject = subjectRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: " + id));

        subjectMapper.updateEntity(request, subject);
        return subjectMapper.toFullResponse(subjectRepository.save(subject));
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        var subject = Subject.builder()
            .name(request.name())
            .description(request.description())
            .build();

        for (var groupId : new HashSet<>(request.groupIds())) {
            subject.getGroups().add(groupReferenceService.getGroupReferenceById(groupId));
        }

        subject = subjectRepository.save(subject);

        var teacher = teacherReferenceService.getTeacherReferenceById(request.teacherId());
        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(true)
            .build();

        permissionRepository.save(permission);

        return subjectMapper.toFullResponse(subject);
    }

    @Override
    public Page<SubjectPageResponse> getPage(SubjectFilter filter, Pageable pageable) {
        // Не-админ всегда видит только свои предметы: teacherId жёстко берётся из токена,
        // что бы клиент ни прислал в фильтре. Админ может смотреть по любому teacherId.
        var effectiveFilter = securityService.isAdmin()
            ? filter
            : new SubjectFilter(
                filter.name(),
                securityService.currentSubjectId().orElseThrow()
            );
        return subjectRepository.findAll(
                new SubjectSpecifications(effectiveFilter).toSpecification(),
                pageable
            )
            .map(subjectMapper::toResponse);
    }
}
