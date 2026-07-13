package com.github.k1mb1.vkr_backend.teacher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherReferenceServiceImplTest {

    @Mock
    TeacherRepository teacherRepository;

    @InjectMocks
    TeacherReferenceServiceImpl service;

    @Test
    void getTeacherReferenceDelegates() {
        var id = UUID.randomUUID();
        var teacher = TeacherEntity.builder().id(id).build();
        when(teacherRepository.getReferenceById(id)).thenReturn(teacher);

        assertThat(service.getTeacherReferenceById(id)).isSameAs(teacher);
    }
}
