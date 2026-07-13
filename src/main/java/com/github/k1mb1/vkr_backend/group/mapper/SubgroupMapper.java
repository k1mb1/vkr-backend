package com.github.k1mb1.vkr_backend.group.mapper;

import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.service.dto.response.SubgroupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubgroupMapper {

    SubgroupResponse toResponse(SubgroupEntity subgroup);
}
