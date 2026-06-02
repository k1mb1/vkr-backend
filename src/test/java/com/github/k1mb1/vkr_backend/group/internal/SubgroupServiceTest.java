package com.github.k1mb1.vkr_backend.group.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubgroupServiceTest {

    @Mock GroupRepository groupRepository;
    @Mock SubgroupRepository subgroupRepository;
    @Mock SubgroupMapper subgroupMapper;

    @InjectMocks SubgroupService service;

    // -----------------------------------------------------------------------
    // getSubgroups — group not found
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsGroupNotFoundThrowsResourceNotFoundException() {
        var groupId = UUID.randomUUID();
        when(groupRepository.existsById(groupId)).thenReturn(false);

        assertThatThrownBy(() -> service.getSubgroups(groupId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(groupId.toString());
    }

    // -----------------------------------------------------------------------
    // getSubgroups — happy path
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsHappyPathReturnsOrderedList() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();

        var sg1 = Subgroup.builder().id(UUID.randomUUID()).index(1).group(group).build();
        var sg2 = Subgroup.builder().id(UUID.randomUUID()).index(2).group(group).build();

        var resp1 = SubgroupResponse.builder().id(sg1.getId()).index(1).build();
        var resp2 = SubgroupResponse.builder().id(sg2.getId()).index(2).build();

        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(subgroupRepository.findByGroupIdOrderByIndex(groupId)).thenReturn(List.of(sg1, sg2));
        when(subgroupMapper.toResponse(sg1)).thenReturn(resp1);
        when(subgroupMapper.toResponse(sg2)).thenReturn(resp2);

        var result = service.getSubgroups(groupId);

        assertThat(result).containsExactly(resp1, resp2);
        verify(subgroupRepository).findByGroupIdOrderByIndex(groupId);
    }

    // -----------------------------------------------------------------------
    // getSubgroups — empty subgroups
    // -----------------------------------------------------------------------

    @Test
    void getSubgroupsReturnsEmptyListWhenNoSubgroups() {
        var groupId = UUID.randomUUID();
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(subgroupRepository.findByGroupIdOrderByIndex(groupId)).thenReturn(List.of());

        var result = service.getSubgroups(groupId);

        assertThat(result).isEmpty();
    }
}
