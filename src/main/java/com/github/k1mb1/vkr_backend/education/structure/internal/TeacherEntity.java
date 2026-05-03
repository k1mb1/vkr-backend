package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.shared.domain.Auditable;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "teachers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @SuperBuilder
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class TeacherEntity extends Auditable {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    UUID id;

    @Column(nullable = false)
    @ToString.Include
    String username;

    @Column(nullable = false)
    @ToString.Include
    String email;
}
