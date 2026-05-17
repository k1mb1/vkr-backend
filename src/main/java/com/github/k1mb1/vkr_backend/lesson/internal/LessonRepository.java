package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository
    extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson> {}
