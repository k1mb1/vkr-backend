package com.github.k1mb1.vkr_backend.subject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.TeacherMapper;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.TeacherResponse;
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

    @Test
    void updatesExistingTeacher() {
        var id = UUID.randomUUID();
        var existing = TeacherEntity.builder().id(id).build();
        when(teacherRepository.findById(id)).thenReturn(Optional.of(existing));
        when(teacherRepository.save(existing)).thenReturn(existing);

        service.createOrUpdateTeacher(
                id,
                CreateOrUpdateTeacherRequest.builder()
                        .username("t")
                        .email("t@e.com")
                        .build());

        verify(teacherMapper).updateEntity(any(), any());
        verify(teacherRepository).save(existing);
    }

    @Test
    void createsTeacherWithGivenIdWhenMissing() {
        var id = UUID.randomUUID();
        when(teacherRepository.findById(id)).thenReturn(Optional.empty());
        when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(teacherMapper.toResponse(any()))
                .thenReturn(TeacherResponse.builder().id(id).build());

        service.createOrUpdateTeacher(
                id,
                CreateOrUpdateTeacherRequest.builder()
                        .username("t")
                        .email("t@e.com")
                        .build());

        var captor = ArgumentCaptor.forClass(TeacherEntity.class);
        verify(teacherRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
    }
}
