package com.github.k1mb1.vkr_backend.domain.student_attendances;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentAttendanceRepository
    extends
        JpaRepository<StudentAttendanceEntity, UUID>,
        JpaSpecificationExecutor<StudentAttendanceEntity>
{
    List<StudentAttendanceEntity> findAllByLesson_Id(UUID lessonId);

    List<StudentAttendanceEntity> findAllByStudent_Id(UUID studentId);

    List<StudentAttendanceEntity> findAllByLesson_Subject_Id(UUID subjectId);

    List<StudentAttendanceEntity> findAllByStudent_IdAndLesson_Subject_Id(
        UUID studentId,
        UUID subjectId
    );
}
