package com.github.k1mb1.vkr_backend.education.attendance.internal;

import com.github.k1mb1.vkr_backend.education.attendance.api.*;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonQueryFacade;
import com.github.k1mb1.vkr_backend.education.structure.api.StructureQueryFacade;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentEntryResponse;
import com.github.k1mb1.vkr_backend.education.subjects.api.SubjectQueryFacade;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAttendanceService {

    final StudentAttendanceRepository attendanceRepository;
    final LessonQueryFacade lessonQueryFacade;
    final SubjectQueryFacade subjectQueryFacade;
    final StructureQueryFacade structureQueryFacade;

    public AttendanceTableResponse findBySubjectId(UUID subjectId, FindAttendanceFilter filter) {
        if (!subjectQueryFacade.existsById(subjectId))
            throw new EntityNotFoundException("Subject not found: " + subjectId);

        var allLessons = filter.groupId() != null
            ? lessonQueryFacade.findBySubjectIdAndGroupId(subjectId, filter.groupId())
            : filter.lessonType() != null
                ? lessonQueryFacade.findBySubjectIdAndType(subjectId, filter.lessonType())
                : lessonQueryFacade.findBySubjectId(subjectId);

        var lessonIds = allLessons.stream().map(l -> l.id()).toList();
        var attendances = attendanceRepository.findByLessonIdIn(lessonIds);

        var lessonEntries = allLessons.stream()
            .map(l -> new AttendanceTableResponse.LessonEntry(l.id(), l.name(), l.dateTime(), l.type(), l.groupId()))
            .toList();

        var cells = attendances.stream()
            .map(a -> new AttendanceCellResponse(a.getId(), a.getLessonId(), a.getStudentId(), a.getPresence(), a.getNote()))
            .toList();

        var studentIds = subjectQueryFacade.findStudentIdsBySubjectId(subjectId);
        var students = structureQueryFacade.findStudentsByIds(studentIds).stream()
            .sorted(Comparator.comparing(s -> s.username()))
            .map(s -> new StudentEntryResponse(s.id(), s.username()))
            .toList();

        return new AttendanceTableResponse(lessonEntries, students, cells);
    }

    @Transactional
    public AttendanceEntryResponse upsert(UUID lessonId, UpsertAttendanceRequest request) {
        if (!lessonQueryFacade.existsById(lessonId))
            throw new EntityNotFoundException("Lesson not found: " + lessonId);
        if (!structureQueryFacade.studentExists(request.studentId()))
            throw new EntityNotFoundException("Student not found: " + request.studentId());

        var attendance = attendanceRepository.findByLessonIdAndStudentId(lessonId, request.studentId())
            .orElseGet(() -> StudentAttendanceEntity.builder().lessonId(lessonId).studentId(request.studentId()).build());

        attendance.setPresence(request.presence());
        if (request.note() != null) attendance.setNote(request.note());

        var saved = attendanceRepository.save(attendance);
        return new AttendanceEntryResponse(saved.getId(), saved.getLessonId(), saved.getPresence(), saved.getNote());
    }
}
