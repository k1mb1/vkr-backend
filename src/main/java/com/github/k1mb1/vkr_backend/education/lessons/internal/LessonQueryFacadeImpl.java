package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.education.lessons.api.LessonInfo;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonQueryFacade;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonQueryFacadeImpl implements LessonQueryFacade {
    final LessonRepository lessonRepository;

    @Override
    public boolean existsById(UUID id) {
        return lessonRepository.existsById(id);
    }

    @Override
    public List<LessonInfo> findBySubjectId(UUID subjectId) {
        return lessonRepository.findAllBySubjectId(subjectId).stream().map(this::toInfo).toList();
    }

    @Override
    public List<LessonInfo> findBySubjectIdAndGroupId(UUID subjectId, UUID groupId) {
        Specification<LessonEntity> spec = (root, query, cb) ->
            cb.and(cb.equal(root.get("subjectId"), subjectId), cb.equal(root.get("groupId"), groupId));
        return lessonRepository.findAll(spec).stream().map(this::toInfo).toList();
    }

    @Override
    public List<LessonInfo> findBySubjectIdAndType(UUID subjectId, LessonType type) {
        Specification<LessonEntity> spec = (root, query, cb) ->
            cb.and(cb.equal(root.get("subjectId"), subjectId), cb.equal(root.get("type"), type));
        return lessonRepository.findAll(spec).stream().map(this::toInfo).toList();
    }

    private LessonInfo toInfo(LessonEntity e) {
        return new LessonInfo(e.getId(), e.getName(), e.getDateTime(), e.getType(),
            e.getSubjectId(), e.getGroupId(), e.getIssuedTaskIndex(), e.getPenaltyMode(), e.getPenaltyStep());
    }
}
