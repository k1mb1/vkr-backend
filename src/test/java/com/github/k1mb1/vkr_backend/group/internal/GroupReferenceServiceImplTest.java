package com.github.k1mb1.vkr_backend.group.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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

    @Mock GroupRepository groupRepository;
    @Mock SubgroupRepository subgroupRepository;

    @InjectMocks GroupReferenceServiceImpl service;

    // -----------------------------------------------------------------------
    // getGroupReferenceById — delegates to groupRepository.getReferenceById
    // -----------------------------------------------------------------------

    @Test
    void getGroupReferenceByIdDelegatesToRepository() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(id).name("G").build();
        when(groupRepository.getReferenceById(id)).thenReturn(group);

        var result = service.getGroupReferenceById(id);

        assertThat(result).isSameAs(group);
        verify(groupRepository).getReferenceById(id);
    }

    // -----------------------------------------------------------------------
    // getSubgroupReferenceById — delegates to subgroupRepository.getReferenceById
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupReferenceByIdDelegatesToRepository() {
        var id = UUID.randomUUID();
        var group = Group.builder().id(UUID.randomUUID()).name("G").build();
        var subgroup = Subgroup.builder().id(id).index(1).group(group).build();
        when(subgroupRepository.getReferenceById(id)).thenReturn(subgroup);

        var result = service.getSubgroupReferenceById(id);

        assertThat(result).isSameAs(subgroup);
        verify(subgroupRepository).getReferenceById(id);
    }
}
