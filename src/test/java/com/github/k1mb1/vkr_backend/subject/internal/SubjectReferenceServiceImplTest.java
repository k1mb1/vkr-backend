package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
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
    void getSubjectReferenceDelegates() {
        var id = UUID.randomUUID();
        var subject = Subject.builder().id(id).name("S").build();
        when(subjectRepository.getReferenceById(id)).thenReturn(subject);

        assertThat(service.getSubjectReferenceById(id)).isSameAs(subject);
    }
}
