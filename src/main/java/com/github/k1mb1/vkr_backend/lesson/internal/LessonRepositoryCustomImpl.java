package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class LessonRepositoryCustomImpl implements LessonRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Lesson> findAllWithDetails(Specification<Lesson> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Lesson> query = cb.createQuery(Lesson.class);
        Root<Lesson> root = query.from(Lesson.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        TypedQuery<Lesson> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint(
            "jakarta.persistence.loadgraph",
            entityManager.getEntityGraph("Lesson.withDetails")
        );

        return typedQuery.getResultList();
    }
}
