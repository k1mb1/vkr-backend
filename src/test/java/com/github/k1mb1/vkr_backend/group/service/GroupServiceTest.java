package com.github.k1mb1.vkr_backend.group.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.mapper.GroupMapper;
import com.github.k1mb1.vkr_backend.group.mapper.StudentMapper;
import com.github.k1mb1.vkr_backend.group.mapper.SubgroupMapper;
import com.github.k1mb1.vkr_backend.group.repository.GroupRepository;
import com.github.k1mb1.vkr_backend.group.repository.SubgroupRepository;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupResponse;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    SubgroupRepository subgroupRepository;

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    StudentService studentService;

    @Mock
    SubgroupMapper subgroupMapper;

    @Mock
    GroupMapper groupMapper;

    @Mock
    StudentMapper studentMapper;

    @InjectMocks
    GroupService service;

    private GroupEntity group(UUID id) {
        return GroupEntity.builder().id(id).name("G").build();
    }

    // ---- attach / detach ----

    @Test
    void attachToSubjectAddsGroup() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>())
                .build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        lenient().when(groupMapper.toResponse(any(), any(), any())).thenReturn(mock(GroupResponse.class));

        service.attachToSubject(groupId, subjectId);

        org.assertj.core.api.Assertions.assertThat(subject.getGroups()).contains(group);
        verify(subjectRepository).save(subject);
    }

    @Test
    void attachToSubjectThrowsConflictWhenAlreadyAttached() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>(Set.of(group)))
                .build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.attachToSubject(groupId, subjectId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already attached");
    }

    @Test
    void attachToSubjectThrowsWhenGroupMissing() {
        var groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachToSubject(groupId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void detachFromSubjectThrowsWhenNotAttached() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>())
                .build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.detachFromSubject(groupId, subjectId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not attached");
    }

    // ---- delete ----

    @Test
    void deleteGroupRemovesStudentsThenGroup() {
        var id = UUID.randomUUID();
        var group = group(id);
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));

        service.deleteGroup(id);

        verify(studentService).deleteStudentsByGroup(id);
        verify(groupRepository).delete(group);
    }

    // ---- createGroup ----

    @Test
    void createGroupCreatesUniqueSubgroupsAndAllStudents() {
        var groupId = UUID.randomUUID();
        when(groupRepository.save(any())).thenReturn(group(groupId));
        when(subgroupRepository.save(any())).thenAnswer(inv -> {
            SubgroupEntity s = inv.getArgument(0);
            return SubgroupEntity.builder()
                    .id(UUID.randomUUID())
                    .index(s.getIndex())
                    .group(s.getGroup())
                    .build();
        });
        lenient().when(groupMapper.toResponse(any(), any(), any())).thenReturn(mock(GroupResponse.class));

        var request = new CreateGroupRequest(
                "G",
                List.of(
                        StudentGroupMemberRequest.builder()
                                .username("A")
                                .subgroupIndex(1)
                                .build(),
                        StudentGroupMemberRequest.builder()
                                .username("B")
                                .subgroupIndex(1)
                                .build(),
                        StudentGroupMemberRequest.builder()
                                .username("C")
                                .subgroupIndex(null)
                                .build()));

        service.createGroup(request);

        // подгруппа с индексом 1 создаётся один раз, несмотря на двух студентов в ней
        verify(subgroupRepository, times(1)).save(any());
        // создаются все три студента
        verify(studentService, times(3)).createStudent(any());
    }
}
