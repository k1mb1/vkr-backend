package com.github.k1mb1.vkr_backend.teacher.repository;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки teacher на группы (group::domain) — reference для scope'ов права. */
public interface TeacherGroupRefRepository extends Repository<GroupEntity, UUID> {

    GroupEntity getReferenceById(UUID id);
}
