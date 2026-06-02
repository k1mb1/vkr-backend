package com.github.k1mb1.vkr_backend.group.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.student.StudentApi;
import com.github.k1mb1.vkr_backend.student.internal.StudentMapper;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
class GroupServiceTest {

    @Mock GroupRepository groupRepository;
    @Mock SubgroupRepository subgroupRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock StudentApi studentApi;
    @Mock SubgroupMapper subgroupMapper;
    @Mock GroupMapper groupMapper;
    @Mock StudentMapper studentMapper;

    @InjectMocks GroupService service;

    // -----------------------------------------------------------------------
    // createGroup
    // -----------------------------------------------------------------------

    @Test
    void createGroupPersistsGroupAndDelegatesStudentCreation() {
        var groupId = UUID.randomUUID();
        var savedGroup = Group.builder().id(groupId).name("ПМИ-101").build();

        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
        when(subgroupRepository.save(any(Subgroup.class))).thenAnswer(inv -> {
            Subgroup sg = inv.getArgument(0);
            return Subgroup.builder().id(UUID.randomUUID()).index(sg.getIndex()).group(sg.getGroup()).build();
        });

        var expected = GroupResponse.builder().id(groupId).name("ПМИ-101").students(List.of()).subgroups(List.of()).build();
        when(groupMapper.toResponse(any(), any(), any())).thenReturn(expected);

        var request = CreateGroupRequest.builder()
            .name("ПМИ-101")
            .students(List.of(
                StudentGroupMemberRequest.builder().username("alice").subgroupIndex(1).build(),
                StudentGroupMemberRequest.builder().username("bob").subgroupIndex(1).build(),
                StudentGroupMemberRequest.builder().username("charlie").subgroupIndex(null).build()
            ))
            .build();

        var result = service.createGroup(request);

        assertThat(result).isSameAs(expected);
        verify(studentApi, times(3)).createStudent(any());
    }

    // -----------------------------------------------------------------------
    // updateGroup
    // -----------------------------------------------------------------------

    @Test
    void updateGroupNotFoundThrows() {
        var id = UUID.randomUUID();
        when(groupRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateGroup(id, UpdateGroupRequest.builder().name("X").build()))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateGroupRenamesWhenNameProvided() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("old").build();
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);
        when(studentApi.findStudentsByGroup(id)).thenReturn(List.of());

        var expected = GroupResponse.builder().id(id).name("new").students(List.of()).subgroups(List.of()).build();
        when(groupMapper.toResponse(any(), any(), any())).thenReturn(expected);

        var result = service.updateGroup(id, UpdateGroupRequest.builder().name("new").students(List.of()).build());

        assertThat(result).isSameAs(expected);
        assertThat(group.getName()).isEqualTo("new");
    }

    @Test
    void updateGroupSkipsRosterWhenStudentsIsNull() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("G1").build();
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        var expected = GroupResponse.builder().id(id).name("G1").students(List.of()).subgroups(List.of()).build();
        when(groupMapper.toResponse(any(), any(), any())).thenReturn(expected);

        // students = null → no roster replacement, no studentApi call
        var result = service.updateGroup(id, UpdateGroupRequest.builder().name("G1").students(null).build());

        assertThat(result).isSameAs(expected);
    }

    // -----------------------------------------------------------------------
    // getGroupById
    // -----------------------------------------------------------------------

    @Test
    void getGroupByIdNotFoundThrows() {
        var id = UUID.randomUUID();
        when(groupRepository.findWithDetailsById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGroupById(id))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getGroupByIdHappyPath() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("ПМИ").build();
        when(groupRepository.findWithDetailsById(id)).thenReturn(Optional.of(group));
        var expected = GroupResponse.builder().id(id).name("ПМИ").students(List.of()).subgroups(List.of()).build();
        when(groupMapper.toResponse(any(), any(), any())).thenReturn(expected);

        var result = service.getGroupById(id);

        assertThat(result).isSameAs(expected);
    }

    // -----------------------------------------------------------------------
    // deleteGroup
    // -----------------------------------------------------------------------

    @Test
    void deleteGroupNotFoundThrows() {
        var id = UUID.randomUUID();
        when(groupRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteGroup(id))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteGroupDeletesStudentsAndGroup() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("G").build();
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));

        service.deleteGroup(id);

        verify(studentApi).deleteStudentsByGroup(id);
        verify(groupRepository).delete(group);
    }

    // -----------------------------------------------------------------------
    // getGroupPage
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getGroupPageDelegatesToRepository() {
        var filter = GroupFilter.builder().name("ПМИ").build();
        var pageable = PageRequest.of(0, 10);
        var group = Group.builder().id(UUID.randomUUID()).name("ПМИ-101").build();
        var pageResponse = GroupPageResponse.builder()
            .id(group.getId()).name("ПМИ-101").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(groupRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(group)));
        when(groupMapper.toPageResponse(group)).thenReturn(pageResponse);

        var result = service.getGroupPage(filter, pageable);

        assertThat(result.getContent()).containsExactly(pageResponse);
    }

    // -----------------------------------------------------------------------
    // attachToSubject
    // -----------------------------------------------------------------------

    @Test
    void attachToSubjectGroupNotFoundThrows() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachToSubject(groupId, subjectId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void attachToSubjectSubjectNotFoundThrows() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachToSubject(groupId, subjectId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void attachToSubjectDuplicateThrowsIllegalState() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        // Subject already contains the group → Set.add returns false
        var subject = Subject.builder().id(subjectId).name("Math").build();
        subject.getGroups().add(group);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.attachToSubject(groupId, subjectId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already attached");
    }

    @Test
    void attachToSubjectHappyPath() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var subject = Subject.builder().id(subjectId).name("Math").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        var expected = GroupResponse.builder().id(groupId).name("G").students(List.of()).subgroups(List.of()).build();
        when(groupMapper.toResponse(any(), any(), any())).thenReturn(expected);

        var result = service.attachToSubject(groupId, subjectId);

        assertThat(result).isSameAs(expected);
        assertThat(subject.getGroups()).contains(group);
        verify(subjectRepository).save(subject);
    }

    // -----------------------------------------------------------------------
    // detachFromSubject
    // -----------------------------------------------------------------------

    @Test
    void detachFromSubjectGroupNotFoundThrows() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detachFromSubject(groupId, subjectId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void detachFromSubjectSubjectNotFoundThrows() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detachFromSubject(groupId, subjectId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void detachFromSubjectNotAttachedThrows() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        // subject does NOT contain group → Set.remove returns false
        var subject = Subject.builder().id(subjectId).name("Math").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.detachFromSubject(groupId, subjectId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("not attached");
    }

    @Test
    void detachFromSubjectHappyPath() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var subject = Subject.builder().id(subjectId).name("Math").build();
        subject.getGroups().add(group);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        service.detachFromSubject(groupId, subjectId);

        assertThat(subject.getGroups()).doesNotContain(group);
        verify(subjectRepository).save(subject);
    }
}
