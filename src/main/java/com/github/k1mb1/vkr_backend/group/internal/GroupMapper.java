package com.github.k1mb1.vkr_backend.group.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupWithSubgroupsResponse;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
interface GroupMapper {

    GroupPageResponse toPageResponse(Group group);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subgroups", source = "subgroups")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "createdAt", source = "group.createdAt")
    @Mapping(target = "updatedAt", source = "group.updatedAt")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    GroupResponse toResponse(Group group, List<SubgroupResponse> subgroups, List<StudentResponse> students);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subgroups", source = "subgroups")
    GroupWithSubgroupsResponse toWithSubgroupsResponse(Group group, List<SubgroupResponse> subgroups);
}
