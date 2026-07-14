package com.github.k1mb1.vkr_backend.group.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.api.GroupSubjectsPort;
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
class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    SubgroupRepository subgroupRepository;

    @Mock
    GroupSubjectsPort groupSubjectsPort;

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
    void attachToSubjectDelegatesToPortAndReturnsGroup() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        lenient().when(groupMapper.toResponse(any(), any(), any())).thenReturn(mock(GroupResponse.class));

        service.attachToSubject(groupId, subjectId);

        verify(groupSubjectsPort).attachGroup(groupId, subjectId);
    }

    @Test
    void attachToSubjectThrowsWhenGroupMissing() {
        var groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachToSubject(groupId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
        verify(groupSubjectsPort, org.mockito.Mockito.never()).attachGroup(any(), any());
    }

    @Test
    void detachFromSubjectDelegatesToPort() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupRepository.existsById(groupId)).thenReturn(true);

        service.detachFromSubject(groupId, subjectId);

        verify(groupSubjectsPort).detachGroup(groupId, subjectId);
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
        // все три студента сохраняются одним батчем (а не тремя точечными save в цикле —
        // это и устраняет прежний N+1 на создании группы).
        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        verify(studentService).createEntities(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue()).hasSize(3);
    }
}
