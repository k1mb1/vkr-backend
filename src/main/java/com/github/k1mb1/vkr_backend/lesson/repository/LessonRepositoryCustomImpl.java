package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
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
    public List<LessonEntity> findAllWithDetails(Specification<LessonEntity> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LessonEntity> query = cb.createQuery(LessonEntity.class);
        Root<LessonEntity> root = query.from(LessonEntity.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        TypedQuery<LessonEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint("jakarta.persistence.loadgraph", entityManager.getEntityGraph("LessonEntity.withDetails"));

        return typedQuery.getResultList();
    }
}
