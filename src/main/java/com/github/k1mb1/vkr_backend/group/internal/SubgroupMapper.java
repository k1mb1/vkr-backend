package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface SubgroupMapper {

    SubgroupResponse toResponse(Subgroup subgroup);
}
