package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectsApi {
    SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request);

    SubjectResponse createSubject(@Valid CreateSubjectRequest request);

    Page<SubjectPageResponse> getPage(SubjectFilter filter, Pageable pageable);
}
