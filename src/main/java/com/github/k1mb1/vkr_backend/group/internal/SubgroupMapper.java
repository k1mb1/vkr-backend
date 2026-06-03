package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import org.mapstruct.Mapper;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = "spring")
interface SubgroupMapper {

    SubgroupResponse toResponse(Subgroup subgroup);
}
