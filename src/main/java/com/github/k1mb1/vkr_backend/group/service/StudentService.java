package com.github.k1mb1.vkr_backend.group.service;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.mapper.StudentMapper;
import com.github.k1mb1.vkr_backend.group.repository.GroupRepository;
import com.github.k1mb1.vkr_backend.group.repository.StudentRepository;
import com.github.k1mb1.vkr_backend.group.repository.SubgroupRepository;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    final StudentRepository studentRepository;

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    final StudentMapper studentMapper;

    public List<StudentResponse> findActiveStudentsByGroup(UUID groupId) {
        return studentRepository.findByGroupIdAndArchivedAtIsNull(groupId).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    /**
     * Загружает сущности студентов группы напрямую (без маппинга в DTO) — пакетно-приватный
     * API для {@link GroupService}, которому при замене состава нужны уже загруженные сущности,
     * чтобы не перечитывать их по одному {@code findById} (бывший N+1 в {@code replaceRoster}).
     */
    List<StudentEntity> findEntitiesByGroup(UUID groupId) {
        return studentRepository.findByGroupId(groupId);
    }

    @Transactional
    public void archiveStudent(UUID studentId) {
        var student = studentRepository
                .findById(studentId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: " + studentId));
        student.archive();
    }

    @Transactional
    public void updateStudent(UUID studentId, UpdateStudentRequest request) {
        var student = studentRepository
                .findById(studentId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: " + studentId));
        studentMapper.updateEntity(request, student);
        if (request.subgroupId() != null) {
            student.setSubgroup(subgroupRepository.getReferenceById(request.subgroupId()));
        }
        studentRepository.save(student);
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        var group = groupRepository.getReferenceById(request.groupId());
        var subgroup = request.subgroupId() != null ? subgroupRepository.getReferenceById(request.subgroupId()) : null;
        var student = StudentEntity.builder()
                .username(request.username())
                .group(group)
                .subgroup(subgroup)
                .build();
        return studentMapper.toResponse(studentRepository.save(student));
    }

    /**
     * Batch-версии ниже — пакетно-приватный API для {@link GroupService}. {@link GroupService}
     * создаёт/обновляет/архивирует студентов целой группы за один вызов; одиночные публичные
     * методы выше при этом порождали N запросов {@code findById}/{@code save} в цикле
     * (бывший N+1 в {@code createGroup}/{@code replaceRoster}). Работаем напрямую на сущностях,
     * уже загруженных в сессию: один {@code saveAll} / один bulk-UPDATE вместо N.
     *
     * <p>Без собственного {@code @Transactional}: эти методы вызываются только из
     * {@code @Transactional}-методов {@link GroupService} и присоединяются к их write-транзакции
     * (propagation REQUIRED). Архитектурное правило требует {@code @Transactional} только на
     * public-методах, а эти намеренно пакетно-приватные.
     */
    void archiveEntities(Collection<StudentEntity> students) {
        var ids = students.stream()
                .map(StudentEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        studentRepository.archiveByIdIn(ids);
        students.forEach(StudentEntity::archive);
    }

    List<StudentEntity> createEntities(Collection<StudentEntity> toCreate) {
        if (toCreate.isEmpty()) {
            return List.of();
        }
        return studentRepository.saveAll(toCreate);
    }

    List<StudentEntity> saveEntities(Collection<StudentEntity> students) {
        if (students.isEmpty()) {
            return List.of();
        }
        return studentRepository.saveAll(students);
    }

    @Transactional
    public void deleteStudentsByGroup(UUID groupId) {
        studentRepository.deleteByGroupId(groupId);
    }
}
