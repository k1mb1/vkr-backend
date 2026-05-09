package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface GroupMapper {

    GroupPageResponse toPageResponse(Group group);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subgroups", source = "subgroups")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "createdAt", source = "group.createdAt")
    @Mapping(target = "updatedAt", source = "group.updatedAt")
    GroupResponse toResponse(Group group, List<SubgroupResponse> subgroups, List<StudentResponse> students);
}

