package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

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

    @InjectMocks
    SubjectService service;

    private final UUID subjectId = UUID.randomUUID();

    // ------------------------------------------------------------------ updateSubject

    @Test
    void updateSubjectThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSubject(subjectId,
            new UpdateSubjectRequest("New Name", null, null)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(subjectId.toString());
    }

    @Test
    void updateSubjectDelegatesToMapperAndRepository() {
        var subject = new Subject();
        var expectedResponse = SubjectResponse.builder().id(subjectId).name("Updated").build();
        var request = new UpdateSubjectRequest("Updated", null, null);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(subjectMapper.toFullResponse(subject)).thenReturn(expectedResponse);

        var result = service.updateSubject(subjectId, request);

        assertThat(result).isSameAs(expectedResponse);
        verify(subjectMapper).updateEntity(request, subject);
        verify(subjectRepository).save(subject);
    }

    // ------------------------------------------------------------------ createSubject

    @Test
    void createSubjectSavesSubjectAndPermission() {
        var teacherId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var request = new CreateSubjectRequest("Math", "Desc", List.of(groupId), teacherId);

        var mockGroup = Group.builder().build();
        var mockTeacher = Teacher.builder().build();
        var savedSubject = Subject.builder().name("Math").build();
        var expectedResponse = SubjectResponse.builder().id(UUID.randomUUID()).name("Math").build();

        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(mockGroup);
        when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);
        when(teacherReferenceService.getTeacherReferenceById(teacherId)).thenReturn(mockTeacher);
        when(permissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(subjectMapper.toFullResponse(savedSubject)).thenReturn(expectedResponse);

        var result = service.createSubject(request);

        assertThat(result).isSameAs(expectedResponse);
        verify(subjectRepository).save(any(Subject.class));
        verify(permissionRepository).save(any());
    }

    // ------------------------------------------------------------------ getPage

    @Test
    @SuppressWarnings("unchecked")
    void getPageDelegatesToRepository() {
        var teacherId = UUID.randomUUID();
        var filter = new SubjectFilter(null, teacherId);
        var pageable = PageRequest.of(0, 20);
        var pageResponse = SubjectPageResponse.builder().id(subjectId).name("Math").build();
        var subject = new Subject();
        var page = new PageImpl<>(List.of(subject), pageable, 1);

        when(subjectRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(page);
        when(subjectMapper.toResponse(subject)).thenReturn(pageResponse);

        var result = service.getPage(filter, pageable);

        assertThat(result.getContent()).containsExactly(pageResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
