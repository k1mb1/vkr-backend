package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface LessonRepository extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson> {
}
