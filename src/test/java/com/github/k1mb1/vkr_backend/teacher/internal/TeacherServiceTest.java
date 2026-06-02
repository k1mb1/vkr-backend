package com.github.k1mb1.vkr_backend.teacher.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    TeacherRepository teacherRepository;

    @Mock
    TeacherMapper teacherMapper;

    @InjectMocks
    TeacherService service;

    private final UUID id = UUID.randomUUID();

    private final CreateOrUpdateTeacherRequest request =
        new CreateOrUpdateTeacherRequest("ivanov", "ivanov@example.com");

    @Test
    void createsNewTeacherWithGivenIdWhenNotFound() {
        when(teacherRepository.findById(id)).thenReturn(Optional.empty());
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));
        var expected = TeacherResponse.builder().id(id).build();
        when(teacherMapper.toResponse(any(Teacher.class))).thenReturn(expected);

        var result = service.createOrUpdateTeacher(id, request);

        assertThat(result).isSameAs(expected);

        var saved = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherMapper).updateEntity(eq(request), saved.capture());
        verify(teacherRepository).save(saved.getValue());
        assertThat(saved.getValue().getId()).isEqualTo(id);
    }

    @Test
    void updatesExistingTeacherWhenFound() {
        var existing = Teacher.builder().id(id).username("old").email("old@example.com").build();
        when(teacherRepository.findById(id)).thenReturn(Optional.of(existing));
        when(teacherRepository.save(existing)).thenReturn(existing);
        var expected = TeacherResponse.builder().id(id).build();
        when(teacherMapper.toResponse(existing)).thenReturn(expected);

        var result = service.createOrUpdateTeacher(id, request);

        assertThat(result).isSameAs(expected);
        verify(teacherMapper).updateEntity(request, existing);
        verify(teacherRepository).save(existing);
    }
}
