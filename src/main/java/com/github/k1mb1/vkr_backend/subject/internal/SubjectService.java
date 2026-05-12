package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectAssignment;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectService
    implements SubjectsApi {

    final SubjectRepository subjectRepository;

    final SubjectOfferingRepository subjectOfferingRepository;

    final SubjectAssignmentRepository subjectAssignmentRepository;

    final SubjectMapper subjectMapper;

    final TeacherReferenceService teacherReferenceService;

    final GroupReferenceService groupReferenceService;

    @Transactional
    @Override
    public SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request) {
        var subject = subjectRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: " + id));

        subjectMapper.updateEntity(request, subject);
        return subjectMapper.toFullResponse(subjectRepository.save(subject));
    }

    @Transactional
    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        var subject = subjectRepository.save(Subject.builder()
                                                 .name(request.name())
                                                 .description(request.description())
                                                 .build());

        var offering = subjectOfferingRepository.save(SubjectOffering.builder()
                                                          .subject(subject)
                                                          .group(groupReferenceService.getGroupReferenceById(
                                                              request.groupId()))
                                                          .build());

        subjectAssignmentRepository.save(SubjectAssignment.builder()
                                             .offering(offering)
                                             .teacher(teacherReferenceService.getTeacherReferenceById(
                                                 request.teacherId()))
                                             .build());

        return subjectMapper.toFullResponse(subject);
    }
}
