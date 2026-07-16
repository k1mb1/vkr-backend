package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля subject на группы (group::domain) — reference для связи предмет—группы. */
public interface SubjectGroupRefRepository extends Repository<GroupEntity, UUID> {

    GroupEntity getReferenceById(UUID id);
}
