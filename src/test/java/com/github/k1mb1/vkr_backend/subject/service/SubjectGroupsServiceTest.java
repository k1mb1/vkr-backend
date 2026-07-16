package com.github.k1mb1.vkr_backend.subject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectGroupRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectGroupsServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectGroupRefRepository groupRefRepository;

    @InjectMocks
    SubjectGroupsService service;

    private GroupEntity group(UUID id) {
        return GroupEntity.builder().id(id).name("G").build();
    }

    @Test
    void attachGroupAddsGroupToSubject() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>())
                .build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(groupRefRepository.getReferenceById(groupId)).thenReturn(group);

        service.attachGroup(groupId, subjectId);

        assertThat(subject.getGroups()).contains(group);
        verify(subjectRepository).save(subject);
    }

    @Test
    void attachGroupThrowsConflictWhenAlreadyAttached() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var group = group(groupId);
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>(Set.of(group)))
                .build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(groupRefRepository.getReferenceById(groupId)).thenReturn(group);

        assertThatThrownBy(() -> service.attachGroup(groupId, subjectId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already attached");
    }

    @Test
    void attachGroupThrowsWhenSubjectMissing() {
        var subjectId = UUID.randomUUID();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachGroup(UUID.randomUUID(), subjectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void detachGroupThrowsWhenNotAttached() {
        var groupId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var subject = SubjectEntity.builder()
                .id(subjectId)
                .name("S")
                .groups(new HashSet<>())
                .build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.detachGroup(groupId, subjectId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not attached");
    }
}
