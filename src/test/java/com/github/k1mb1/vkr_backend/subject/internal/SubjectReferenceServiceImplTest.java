package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectReferenceServiceImplTest {

    @Mock
    SubjectRepository subjectRepository;

    @InjectMocks
    SubjectReferenceServiceImpl service;

    @Test
    void getSubjectReferenceByIdDelegatesToRepository() {
        var id = UUID.randomUUID();
        var subject = Subject.builder().name("Math").build();

        when(subjectRepository.getReferenceById(id)).thenReturn(subject);

        var result = service.getSubjectReferenceById(id);

        assertThat(result).isSameAs(subject);
        verify(subjectRepository).getReferenceById(id);
    }
}
