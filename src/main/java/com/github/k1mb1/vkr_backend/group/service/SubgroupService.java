package com.github.k1mb1.vkr_backend.group.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.mapper.SubgroupMapper;
import com.github.k1mb1.vkr_backend.group.repository.GroupRepository;
import com.github.k1mb1.vkr_backend.group.repository.SubgroupRepository;
import com.github.k1mb1.vkr_backend.group.service.dto.response.SubgroupResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubgroupService {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    final SubgroupMapper subgroupMapper;

    public List<SubgroupResponse> getSubgroups(UUID groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", groupId);
        }
        return subgroupRepository.findByGroupIdOrderByIndex(groupId).stream()
                .map(subgroupMapper::toResponse)
                .toList();
    }
}
