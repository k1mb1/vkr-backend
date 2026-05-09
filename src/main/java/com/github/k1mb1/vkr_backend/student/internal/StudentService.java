package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional
    public void createStudentsForGroup(
        Group group,
        Map<Short, Subgroup> indexToSubgroup,
        List<StudentGroupMemberRequest> students
    ) {
        for (var studentReq : students) {
            var student = Student.builder()
                .username(studentReq.username())
                .group(group)
                .subgroup(
                    studentReq.subgroupIndex() != null
                        ? indexToSubgroup.get(studentReq.subgroupIndex())
                        : null
                )
                .build();
            studentRepository.save(student);
        }
    }
}
