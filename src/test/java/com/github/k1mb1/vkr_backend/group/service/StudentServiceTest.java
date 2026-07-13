package com.github.k1mb1.vkr_backend.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.mapper.StudentMapper;
import com.github.k1mb1.vkr_backend.group.repository.StudentRepository;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
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

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    StudentRepository studentRepository;

    @Mock
    GroupReferenceService groupReferenceService;

    @Mock
    StudentMapper studentMapper;

    @InjectMocks
    StudentService service;

    @Test
    void createStudentWithSubgroup() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = GroupEntity.builder().id(groupId).name("G").build();
        var subgroup =
                SubgroupEntity.builder().id(subgroupId).index(1).group(group).build();
        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(group);
        when(groupReferenceService.getSubgroupReferenceById(subgroupId)).thenReturn(subgroup);
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createStudent(CreateStudentRequest.builder()
                .username("Иванов")
                .groupId(groupId)
                .subgroupId(subgroupId)
                .build());

        var captor = ArgumentCaptor.forClass(StudentEntity.class);
        verify(studentRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("Иванов");
        assertThat(saved.getGroup()).isEqualTo(group);
        assertThat(saved.getSubgroup()).isEqualTo(subgroup);
    }

    @Test
    void createStudentWithoutSubgroupDoesNotResolveSubgroup() {
        var groupId = UUID.randomUUID();
        var group = GroupEntity.builder().id(groupId).name("G").build();
        when(groupReferenceService.getGroupReferenceById(groupId)).thenReturn(group);
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createStudent(CreateStudentRequest.builder()
                .username("Петров")
                .groupId(groupId)
                .subgroupId(null)
                .build());

        var captor = ArgumentCaptor.forClass(StudentEntity.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue().getSubgroup()).isNull();
    }

    @Test
    void archiveStudentMarksArchived() {
        var id = UUID.randomUUID();
        var student = StudentEntity.builder().id(id).username("S").build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        service.archiveStudent(id);

        assertThat(student.isArchived()).isTrue();
    }

    @Test
    void archiveStudentThrowsWhenMissing() {
        var id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archiveStudent(id)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateStudentDelegatesToMapperAndSaves() {
        var id = UUID.randomUUID();
        var student = StudentEntity.builder().id(id).username("S").build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        service.updateStudent(id, UpdateStudentRequest.builder().username("New").build());

        verify(studentMapper).updateEntity(any(), any(), any());
        verify(studentRepository).save(student);
    }

    @Test
    void findActiveStudentsByGroupMapsResults() {
        var groupId = UUID.randomUUID();
        var student =
                StudentEntity.builder().id(UUID.randomUUID()).username("S").build();
        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId)).thenReturn(List.of(student));
        var response = StudentResponse.builder()
                .id(student.getId())
                .username("S")
                .groupId(groupId)
                .build();
        when(studentMapper.toResponse(student)).thenReturn(response);

        assertThat(service.findActiveStudentsByGroup(groupId)).containsExactly(response);
    }

    @Test
    void deleteStudentsByGroupDelegates() {
        var groupId = UUID.randomUUID();
        service.deleteStudentsByGroup(groupId);
        verify(studentRepository).deleteByGroupId(groupId);
    }
}
