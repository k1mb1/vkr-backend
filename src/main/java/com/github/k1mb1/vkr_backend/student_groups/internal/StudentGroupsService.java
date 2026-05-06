package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.StudentGroupsApi;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StudentGroupsService implements StudentGroupsApi {

    final StudentGroupsRepository groupRepository;
    final StudentGroupsSpecBuilder specBuilder;

    @Override
    public Page<StudentGroupListDto> findAll(StudentGroupFilterRequest filter, Pageable pageable) {
        return groupRepository.findAll(specBuilder.build(filter), pageable)
                .map(group -> new StudentGroupListDto(
                        group.getId(),
                        group.getName(),
                        group.getSubgroupCount(),
                        group.getTotalStudentCount()
                ));
    }

}
