package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.student.StudentResponse;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    StudentResponse toResponse(Student student);
}
