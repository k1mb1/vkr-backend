package com.github.k1mb1.vkr_backend.group.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupReferenceServiceImplTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    SubgroupRepository subgroupRepository;

    @InjectMocks
    GroupReferenceServiceImpl service;

    @Test
    void getGroupReferenceDelegates() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("G").build();
        when(groupRepository.getReferenceById(id)).thenReturn(group);

        assertThat(service.getGroupReferenceById(id)).isSameAs(group);
    }

    @Test
    void getSubgroupReferenceDelegates() {
        var id = UUID.randomUUID();
        var subgroup = Subgroup.builder().id(id).index(1).build();
        when(subgroupRepository.getReferenceById(id)).thenReturn(subgroup);

        assertThat(service.getSubgroupReferenceById(id)).isSameAs(subgroup);
    }

    @Test
    void resolveAudienceNullGroupReturnsEmptyRefWithoutLookup() {
        var ref = service.resolveAudience(null, null);

        assertThat(ref.group()).isNull();
        assertThat(ref.allowedSubgroup()).isNull();
        org.mockito.Mockito.verifyNoInteractions(groupRepository, subgroupRepository);
    }

    @Test
    void resolveAudienceResolvesGroupAndSubgroup() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(group).build();
        when(groupRepository.getReferenceById(groupId)).thenReturn(group);
        when(subgroupRepository.getReferenceById(subgroupId)).thenReturn(subgroup);

        var ref = service.resolveAudience(groupId, subgroupId);

        assertThat(ref.group()).isSameAs(group);
        assertThat(ref.allowedSubgroup()).isSameAs(subgroup);
    }

    @Test
    void resolveAudienceRejectsSubgroupFromAnotherGroup() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var foreignGroup = Group.builder().id(UUID.randomUUID()).name("Other").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(foreignGroup).build();
        when(groupRepository.getReferenceById(groupId)).thenReturn(group);
        when(subgroupRepository.getReferenceById(subgroupId)).thenReturn(subgroup);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            service.resolveAudience(groupId, subgroupId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Subgroup does not belong");
    }
}
