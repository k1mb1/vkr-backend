package com.github.k1mb1.vkr_backend.subject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.auth.SecurityService;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.subject.api.OwnerPermissionGranter;
import com.github.k1mb1.vkr_backend.subject.api.SubjectVisibilityPort;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectGroupRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.filter.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.SubjectResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectMapper subjectMapper;

    @Mock
    SubjectGroupRefRepository groupRefRepository;

    @Mock
    OwnerPermissionGranter ownerPermissionGranter;

    @Mock
    SubjectVisibilityPort subjectVisibility;

    @Mock
    SecurityService securityService;

    @InjectMocks
    SubjectService service;

    @Test
    void createSubjectAttachesGroupsAndGrantsAllPermissionsToTeacher() {
        var teacherId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupRefRepository.getReferenceById(groupId))
                .thenReturn(GroupEntity.builder().id(groupId).name("G").build());
        when(subjectRepository.save(any())).thenAnswer(inv -> {
            SubjectEntity s = inv.getArgument(0);
            s.setId(subjectId);
            return s;
        });
        lenient().when(subjectMapper.toFullResponse(any())).thenReturn(mock(SubjectResponse.class));

        service.createSubject(new CreateSubjectRequest("Math", "desc", List.of(groupId), teacherId));

        // авто-грант владельцу делегируется порту teacher, а не пишется здесь
        verify(ownerPermissionGranter).grantAllPermissions(teacherId, subjectId);
    }

    @Test
    void updateSubjectThrowsWhenMissing() {
        var id = UUID.randomUUID();
        when(subjectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSubject(id, mock(UpdateSubjectRequest.class)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateSubjectSavesAndMaps() {
        var id = UUID.randomUUID();
        var subject = SubjectEntity.builder().id(id).name("S").build();
        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        service.updateSubject(id, mock(UpdateSubjectRequest.class));

        verify(subjectMapper).updateEntity(any(), any());
        verify(subjectRepository).save(subject);
    }

    @Test
    void getPageForNonAdminForcesTeacherFromToken() {
        var tokenTeacherId = UUID.randomUUID();
        when(securityService.isAdmin()).thenReturn(false);
        when(securityService.currentSubjectId()).thenReturn(Optional.of(tokenTeacherId));
        when(subjectVisibility.visibleSubjectIds(tokenTeacherId)).thenReturn(Set.of());
        when(subjectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        var result = service.getPage(new SubjectFilter("q", UUID.randomUUID()), Pageable.unpaged());

        assertThat(result).isEmpty();
        // не-админ не может подменить teacherId — берётся из токена, видимость приходит из порта
        verify(subjectVisibility).visibleSubjectIds(tokenTeacherId);
        verify(securityService).currentSubjectId();
    }
}
