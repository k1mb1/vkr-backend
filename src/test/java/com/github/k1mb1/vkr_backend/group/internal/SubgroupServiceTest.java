package com.github.k1mb1.vkr_backend.group.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    GroupRepository groupRepository;

    @Mock
    SubgroupRepository subgroupRepository;

    @Mock
    SubgroupMapper subgroupMapper;

    @InjectMocks
    SubgroupService service;

    @Test
    void getSubgroupsMapsOrderedResult() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("G").build();
        var subgroup = Subgroup.builder().id(UUID.randomUUID()).index(1).group(group).build();
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(subgroupRepository.findByGroupIdOrderByIndex(groupId)).thenReturn(List.of(subgroup));
        var response = SubgroupResponse.builder().id(subgroup.getId()).index(1).build();
        when(subgroupMapper.toResponse(subgroup)).thenReturn(response);

        assertThat(service.getSubgroups(groupId)).containsExactly(response);
    }

    @Test
    void getSubgroupsThrowsWhenGroupMissing() {
        var groupId = UUID.randomUUID();
        when(groupRepository.existsById(groupId)).thenReturn(false);

        assertThatThrownBy(() -> service.getSubgroups(groupId))
            .isInstanceOf(ResourceNotFoundException.class);
        org.mockito.Mockito.verify(subgroupRepository, org.mockito.Mockito.never())
            .findByGroupIdOrderByIndex(any());
    }
}
