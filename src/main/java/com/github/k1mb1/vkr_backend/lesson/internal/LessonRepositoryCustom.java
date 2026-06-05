package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public interface LessonRepositoryCustom {

    List<Lesson> findAllWithDetails(Specification<Lesson> spec);
}
