package com.github.k1mb1.vkr_backend.education.structure.api;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StructureQueryFacade {
    boolean studentExists(UUID id);
    boolean teacherExists(UUID id);
    boolean groupExists(UUID id);
    List<StudentSummary> findStudentsByIds(Collection<UUID> ids);
    List<UUID> findAllStudentIdsByGroupId(UUID groupId);
    String getGroupNameById(UUID groupId);
}
