package com.github.k1mb1.vkr_backend.education.subjects.api;

import java.util.List;
import java.util.UUID;

public interface SubjectQueryFacade {
    boolean existsById(UUID id);
    String getNameById(UUID id);
    List<UUID> findStudentIdsBySubjectId(UUID subjectId);
}
