package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля lesson на группы (group::domain) — только получение reference для FK scope'ов. */
public interface LessonGroupRefRepository extends Repository<GroupEntity, UUID> {

    GroupEntity getReferenceById(UUID id);
}
