package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public interface LessonRepositoryCustom {

    List<LessonEntity> findAllWithDetails(Specification<LessonEntity> spec);
}
