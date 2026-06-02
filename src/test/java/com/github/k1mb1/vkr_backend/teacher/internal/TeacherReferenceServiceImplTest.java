package com.github.k1mb1.vkr_backend.teacher.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
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
    void getTeacherReferenceByIdDelegatesToRepository() {
        var id = UUID.randomUUID();
        var teacher = Teacher.builder().id(id).username("prof_smith").email("smith@uni.edu").build();

        when(teacherRepository.getReferenceById(id)).thenReturn(teacher);

        var result = service.getTeacherReferenceById(id);

        assertThat(result).isSameAs(teacher);
        verify(teacherRepository).getReferenceById(id);
    }
}
