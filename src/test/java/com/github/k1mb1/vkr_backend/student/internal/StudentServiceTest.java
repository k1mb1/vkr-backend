package com.github.k1mb1.vkr_backend.student.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock StudentRepository studentRepository;
    @Mock GroupReferenceService groupReferenceService;
    @Mock StudentMapper studentMapper;

    @InjectMocks StudentService service;

    // -----------------------------------------------------------------------
    // createStudent — without subgroup
    // -----------------------------------------------------------------------

    @Test
    void createStudentWithoutSubgroupPersistsStudent() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var savedStudent = Student.builder().id(UUID.randomUUID()).username("alice").group(group).build();
        var expectedResponse = StudentResponse.builder()
            .id(savedStudent.getId()).username("alice").groupId(groupId).build();

        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(group);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
        when(studentMapper.toResponse(savedStudent)).thenReturn(expectedResponse);

        var request = CreateStudentRequest.builder()
            .username("alice")
            .groupId(groupId)
            .subgroupId(null)
            .build();

        var result = service.createStudent(request);

        assertThat(result).isSameAs(expectedResponse);
        verify(groupReferenceService).getGroupReferenceById(groupId);
        verify(studentRepository).save(any(Student.class));
    }

    // -----------------------------------------------------------------------
    // createStudent — with subgroup
    // -----------------------------------------------------------------------

    @Test
    void createStudentWithSubgroupLooksUpSubgroupReference() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(group).build();
        var savedStudent = Student.builder()
            .id(UUID.randomUUID()).username("bob").group(group).subgroup(subgroup).build();
        var expectedResponse = StudentResponse.builder()
            .id(savedStudent.getId()).username("bob").groupId(groupId).subgroupId(subgroupId).build();

        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(group);
        when(groupReferenceService.getSubgroupReferenceById(subgroupId)).thenReturn(subgroup);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
        when(studentMapper.toResponse(savedStudent)).thenReturn(expectedResponse);

        var request = CreateStudentRequest.builder()
            .username("bob")
            .groupId(groupId)
            .subgroupId(subgroupId)
            .build();

        var result = service.createStudent(request);

        assertThat(result).isSameAs(expectedResponse);
        verify(groupReferenceService).getSubgroupReferenceById(subgroupId);
    }

    // -----------------------------------------------------------------------
    // archiveStudent — not found
    // -----------------------------------------------------------------------

    @Test
    void archiveStudentNotFoundThrowsEntityNotFoundException() {
        var id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archiveStudent(id))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    // -----------------------------------------------------------------------
    // archiveStudent — happy path
    // -----------------------------------------------------------------------

    @Test
    void archiveStudentSetsArchivedAt() {
        var id = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var student = Student.builder().id(id).username("alice").group(group).build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        service.archiveStudent(id);

        assertThat(student.isArchived()).isTrue();
    }

    // -----------------------------------------------------------------------
    // updateStudent — not found
    // -----------------------------------------------------------------------

    @Test
    void updateStudentNotFoundThrowsEntityNotFoundException() {
        var id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        var request = UpdateStudentRequest.builder().username("x").build();
        assertThatThrownBy(() -> service.updateStudent(id, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    // -----------------------------------------------------------------------
    // updateStudent — happy path
    // -----------------------------------------------------------------------

    @Test
    void updateStudentHappyPathInvokesMappingAndSaves() {
        var id = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var student = Student.builder().id(id).username("old").group(group).build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        var request = UpdateStudentRequest.builder().username("new").build();
        service.updateStudent(id, request);

        verify(studentMapper).updateEntity(request, student, groupReferenceService);
        verify(studentRepository).save(student);
    }

    // -----------------------------------------------------------------------
    // findStudentsByGroup
    // -----------------------------------------------------------------------

    @Test
    void findStudentsByGroupDelegatesToRepository() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var student = Student.builder().id(UUID.randomUUID()).username("alice").group(group).build();
        var response = StudentResponse.builder()
            .id(student.getId()).username("alice").groupId(groupId).build();

        when(studentRepository.findByGroupId(groupId)).thenReturn(List.of(student));
        when(studentMapper.toResponse(student)).thenReturn(response);

        var result = service.findStudentsByGroup(groupId);

        assertThat(result).containsExactly(response);
        verify(studentRepository).findByGroupId(groupId);
    }

    // -----------------------------------------------------------------------
    // findActiveStudentsByGroup
    // -----------------------------------------------------------------------

    @Test
    void findActiveStudentsByGroupReturnsOnlyNonArchivedStudents() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var activeStudent = Student.builder().id(UUID.randomUUID()).username("active").group(group).build();
        var response = StudentResponse.builder()
            .id(activeStudent.getId()).username("active").groupId(groupId).build();

        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId)).thenReturn(List.of(activeStudent));
        when(studentMapper.toResponse(activeStudent)).thenReturn(response);

        var result = service.findActiveStudentsByGroup(groupId);

        assertThat(result).containsExactly(response);
        verify(studentRepository).findByGroupIdAndArchivedAtIsNull(groupId);
    }

    // -----------------------------------------------------------------------
    // deleteStudentsByGroup
    // -----------------------------------------------------------------------

    @Test
    void deleteStudentsByGroupDelegatesToRepository() {
        var groupId = UUID.randomUUID();

        service.deleteStudentsByGroup(groupId);

        verify(studentRepository).deleteByGroupId(groupId);
    }
}
