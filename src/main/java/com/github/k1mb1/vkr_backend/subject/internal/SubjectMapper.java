package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface SubjectMapper {

    SubjectPageResponse toResponse(Subject subject);
}

