package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.subject.SubjectAssignmentApi;
import com.github.k1mb1.vkr_backend.subject.SubjectOfferingReferenceService;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectAssignmentRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectAssignmentResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectAssignmentService
    implements SubjectAssignmentApi {

    final SubjectAssignmentRepository subjectAssignmentRepository;

    final TeacherReferenceService teacherReferenceService;

    final SubjectOfferingReferenceService subjectOfferingReferenceService;

    final GroupReferenceService groupReferenceService;

    final SubjectMapper subjectMapper;

    //    @Transactional
    //    @Override
    //    public SubjectAssignmentResponse create(CreateSubjectAssignmentRequest request) {
    //        var subgroup = request.subgroupId() != null
    //                       ? groupReferenceService.getSubgroupReferenceById(request.subgroupId())
    //                       : null;
    //
    //        var assignment = SubjectAssignment.builder()
    //            .teacher(teacherReferenceService.getTeacherReferenceById(request.teacherId()))
    //            .offering(subjectOfferingReferenceService.findById(request.offeringId()))
    //            .subgroup(subgroup)
    //            .lessonTypeScope(request.lessonTypeScope())
    //            .build();
    //
    //        return subjectMapper.toAssignmentResponse(subjectAssignmentRepository.save(assignment));
    //    }

    @Transactional
    @Override
    public SubjectAssignmentResponse update(UUID id, UpdateSubjectAssignmentRequest request) {
        var assignment = subjectAssignmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("SubjectAssignment not found: " + id));

        assignment.setTeacher(teacherReferenceService.getTeacherReferenceById(request.teacherId()));
        assignment.setSubgroup(request.subgroupId() != null
                               ? groupReferenceService.getSubgroupReferenceById(request.subgroupId())
                               : null);
        assignment.setLessonTypeScope(request.lessonTypeScope());

        return subjectMapper.toAssignmentResponse(subjectAssignmentRepository.save(assignment));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        if (!subjectAssignmentRepository.existsById(id)) {
            throw new EntityNotFoundException("SubjectAssignment not found: " + id);
        }
        subjectAssignmentRepository.deleteById(id);
    }
}