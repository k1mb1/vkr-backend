package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.security.SecurityService;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @Mock
    SubjectMapper subjectMapper;

    @Mock
    TeacherReferenceService teacherReferenceService;

    @Mock
    GroupReferenceService groupReferenceService;

    @Mock
    SecurityService securityService;

    @InjectMocks
    SubjectService service;

    @Test
    void createSubjectAttachesGroupsAndGrantsAllPermissionsToTeacher() {
        var teacherId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupReferenceService.getGroupReferenceById(groupId))
            .thenReturn(Group.builder().id(groupId).name("G").build());
        when(subjectRepository.save(any()))
            .thenAnswer(inv -> {
                Subject s = inv.getArgument(0);
                s.setId(subjectId);
                return s;
            });
        when(teacherReferenceService.getTeacherReferenceById(teacherId))
            .thenReturn(Teacher.builder().id(teacherId).build());
        lenient().when(subjectMapper.toFullResponse(any())).thenReturn(mock(SubjectResponse.class));

        service.createSubject(new CreateSubjectRequest("Math", "desc", List.of(groupId), teacherId));

        var captor = ArgumentCaptor.forClass(TeacherSubjectPermission.class);
        verify(permissionRepository).save(captor.capture());
        assertThat(captor.getValue().isAllPermissions()).isTrue();
        assertThat(captor.getValue().getSubject().getId()).isEqualTo(subjectId);
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
        var subject = Subject.builder().id(id).name("S").build();
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
        when(subjectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        var result = service.getPage(
            new com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter("q", UUID.randomUUID()),
            Pageable.unpaged()
        );

        assertThat(result).isEmpty();
        // не-админ не может подменить teacherId — берётся из токена
        verify(securityService).currentSubjectId();
    }
}
