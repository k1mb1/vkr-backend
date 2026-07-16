package com.github.k1mb1.vkr_backend.group.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupWithSubgroupsResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.SubgroupResponse;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface GroupMapper {

    GroupPageResponse toPageResponse(GroupEntity group);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subgroups", source = "subgroups")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "createdAt", source = "group.createdAt")
    @Mapping(target = "updatedAt", source = "group.updatedAt")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    GroupResponse toResponse(GroupEntity group, List<SubgroupResponse> subgroups, List<StudentResponse> students);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subgroups", source = "subgroups")
    GroupWithSubgroupsResponse toWithSubgroupsResponse(GroupEntity group, List<SubgroupResponse> subgroups);
}
