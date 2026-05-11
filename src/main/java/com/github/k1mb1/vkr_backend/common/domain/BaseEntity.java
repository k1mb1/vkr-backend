package com.github.k1mb1.vkr_backend.common.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;

@Hidden
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity extends Auditable {

    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> objectEffectiveClass =
            o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != objectEffectiveClass) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy
                  .getHibernateLazyInitializer()
                  .getPersistentClass()
                  .hashCode()
            : getClass().hashCode();
    }
}
