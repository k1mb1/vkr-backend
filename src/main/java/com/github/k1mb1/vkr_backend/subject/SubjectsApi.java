package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectsApi {
    SubjectResponse update(UUID id, UpdateSubjectRequest request);

    Page<SubjectPageResponse> getPage(SubjectFilter filter, Pageable pageable);

    SubjectResponse create(@Valid CreateSubjectRequest request);
}
