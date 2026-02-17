package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupDetailResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentGroupMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final StudentGroupMapper groupMapper;

    public Page<StudentGroupResponse> findAll(StudentGroupFilter filter, Pageable pageable) {
        return groupRepository.findAll(filter.toSpecification(), pageable)
                .map(groupMapper::toResponse);
    }

    public StudentGroupDetailResponse findById(UUID id) {
        return groupRepository.findWithStudentsById(id)
                .map(groupMapper::toDetailResponse)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));
    }

    public StudentGroupEntity findEntityById(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));
    }

    @Transactional
    public StudentGroupResponse create(CreateStudentGroupRequest request) {
        var entity = groupMapper.toEntity(request);
        return groupMapper.toResponse(groupRepository.save(entity));
    }

    @Transactional
    public StudentGroupResponse update(UUID id, UpdateStudentGroupRequest request) {
        var entity = groupRepository.findWithStudentsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));
        groupMapper.update(entity, request);
        return groupMapper.toResponse(groupRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!groupRepository.existsById(id)) {
            throw new EntityNotFoundException("Group not found: " + id);
        }
        groupRepository.deleteById(id);
    }
}