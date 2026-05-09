package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findByGroup(Group group);

    List<Student> findByGroupAndArchivedAtIsNull(Group group);
}
