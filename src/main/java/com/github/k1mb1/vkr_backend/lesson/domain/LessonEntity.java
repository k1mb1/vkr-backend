package com.github.k1mb1.vkr_backend.lesson.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Hidden
@Entity
@Table(
        name = "lessons",
        indexes = {
            @Index(name = "idx_lessons_subject_id", columnList = "subject_id"),
        })
@SQLRestriction("archived_at IS NULL")
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Lesson.withDetails",
            attributeNodes = {
                @NamedAttributeNode("subject"),
                // scopes.group / scopes.allowedSubgroup тянутся здесь же: list-read-пути
                // (getLessons, getAttendanceTable, getGradingTable) проходят по видимым scope'ам
                // и читают их группу/подгруппу — без подграфа это N лениких SELECT'ов на запрос.
                @NamedAttributeNode(value = "scopes", subgraph = "Lesson.withDetails.scopes"),
            },
            subgraphs =
                    @NamedSubgraph(
                            name = "Lesson.withDetails.scopes",
                            attributeNodes = {
                                @NamedAttributeNode("group"),
                                @NamedAttributeNode("allowedSubgroup"),
                            })),
    @NamedEntityGraph(
            name = "Lesson.withFullDetails",
            attributeNodes = {
                @NamedAttributeNode(value = "subject", subgraph = "Subject.withGroups"),
                @NamedAttributeNode("scopes"),
            },
            subgraphs = @NamedSubgraph(name = "Subject.withGroups", attributeNodes = @NamedAttributeNode("groups"))),
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LessonEntity extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    SubjectEntity subject;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "lesson_type", nullable = false, columnDefinition = "lesson_type")
    LessonType type;

    @Column(name = "order_index", nullable = false)
    int orderIndex;

    @Column(columnDefinition = "text")
    String topic;

    // Маркер «текущего/активного» занятия — точка отсчёта для понижения балла.
    // Не более одного активного занятия на предмет (partial unique index).
    @Column(name = "active", nullable = false)
    boolean active;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    Set<LessonScopeEntity> scopes = new HashSet<>();
}
