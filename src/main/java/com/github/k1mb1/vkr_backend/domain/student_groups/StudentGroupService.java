package com.github.k1mb1.vkr_backend.domain.student_groups;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGroupService {

    final StudentGroupRepository groupRepository;

    public StudentGroupEntity findEntityById(UUID id) {
        return groupRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Group not found: " + id)
            );
    }
}
