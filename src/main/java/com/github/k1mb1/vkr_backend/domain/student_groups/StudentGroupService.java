package com.github.k1mb1.vkr_backend.domain.student_groups;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGroupService {

    final StudentGroupRepository groupRepository;

    public StudentGroupEntity findEntityById(UUID id) {
        return groupRepository
                .findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("User", id))
                );
    }

    public Page<StudentGroupPageResponse> findAll(Pageable pageable) {
        return groupRepository.findAllMainGroupsWithStudentCount(pageable);
    }


    @Transactional
    public StudentGroupResponse create(CreateGroupRequest request) {
        var groupName = request.groupName().trim();
        boolean useSubgroups = request.studentNames().size() > 1;

        var savedMainGroup = groupRepository.save(
            StudentGroupEntity.builder().name(groupName).build()
        );

        var subgroupResponses = new ArrayList<SubgroupResponse>();
        if (useSubgroups) {
            groupRepository.insertSubgroupsBulk(
                savedMainGroup.getId(),
                groupName,
                request.studentNames().size()
            );

            for (int i = 0; i < request.studentNames().size(); i++) {
                subgroupResponses.add(
                    new SubgroupResponse(null, groupName + "/" + (i + 1), List.of())
                );
            }
        }

        return new StudentGroupResponse(
            savedMainGroup.getId(),
            groupName,
            subgroupResponses
        );
    }

//    @Transactional
//    public StudentGroupResponse create(CreateGroupRequest request) {
//        try {
//            return doCreate(request);
//        } catch (DataIntegrityViolationException ex) {
//            throw new ResponseStatusException(
//                HttpStatus.CONFLICT,
//                "Group already exists: " + request.groupName().trim()
//            );
//        }
//    }
//
//    private StudentGroupResponse doCreate(CreateGroupRequest request) {
//        var groupName = request.groupName().trim();
//
//        boolean useSubgroups = request.studentNames().size() > 1;
//
//        // 1. INSERT main group — uniqueness guaranteed by DB index
//        var mainGroup = groupRepository.save(
//            StudentGroupEntity.builder().name(groupName).build()
//        );
//        // Flush now so subgroup INSERTs (which reference mainGroup via FK)
//        // are sent as a clean separate batch — avoids HHH90032022 circular sort warning.
//        entityManager.flush();
//
//        // 2. Batch INSERT subgroups if needed
//        var subgroupByIndex = new HashMap<Integer, StudentGroupEntity>();
//        if (useSubgroups) {
//            var toCreate = new ArrayList<StudentGroupEntity>();
//            for (int i = 0; i < request.studentNames().size(); i++) {
//                toCreate.add(
//                    StudentGroupEntity.builder()
//                        .name(groupName + "/" + (i + 1))
//                        .parentGroup(mainGroup)
//                        .build()
//                );
//            }
//            var saved = groupRepository.saveAll(toCreate);
//            for (int i = 0; i < saved.size(); i++) {
//                subgroupByIndex.put(i, saved.get(i));
//            }
//        }
//
//        // 3. One SELECT for all existing students
//        var allUsernames = request
//            .studentNames()
//            .stream()
//            .flatMap(List::stream)
//            .map(String::trim)
//            .filter(u -> !u.isBlank())
//            .distinct()
//            .toList();
//
//        var existingByUsername = new HashMap<String, StudentEntity>();
//        studentRepository
//            .findAllByUsernameIn(allUsernames)
//            .forEach(s -> existingByUsername.put(s.getUsername().trim(), s));
//
//        // 4. Batch INSERT new students + batch UPDATE existing ones' group
//        var toInsert = new ArrayList<StudentEntity>();
//        // group → list of existing student ids that need their group updated
//        var toUpdate = new HashMap<StudentGroupEntity, List<UUID>>();
//
//        for (int i = 0; i < request.studentNames().size(); i++) {
//            var targetGroup = useSubgroups ? subgroupByIndex.get(i) : mainGroup;
//            for (var raw : request.studentNames().get(i)) {
//                var username = raw.trim();
//                if (username.isBlank()) continue;
//                if (!existingByUsername.containsKey(username)) {
//                    var s = StudentEntity.builder()
//                        .username(username)
//                        .group(targetGroup)
//                        .build();
//                    toInsert.add(s);
//                    existingByUsername.put(username, s);
//                } else {
//                    // Collect ids for batch UPDATE instead of dirty-tracking
//                    toUpdate
//                        .computeIfAbsent(targetGroup, k -> new ArrayList<>())
//                        .add(existingByUsername.get(username).getId());
//                }
//            }
//        }
//        if (!toInsert.isEmpty()) {
//            studentRepository.saveAll(toInsert);
//        }
//        // One UPDATE per distinct target group (usually 1-2 groups)
//        toUpdate.forEach((group, ids) ->
//            studentRepository.updateGroupForIds(group, ids)
//        );
//
//        // 5. Build response from in-memory data — no extra SELECT
//        var subgroupResponses = new ArrayList<SubgroupResponse>();
//        if (useSubgroups) {
//            for (int i = 0; i < request.studentNames().size(); i++) {
//                var sg = subgroupByIndex.get(i);
//                var students = request
//                    .studentNames()
//                    .get(i)
//                    .stream()
//                    .map(String::trim)
//                    .filter(u -> !u.isBlank())
//                    .map(existingByUsername::get)
//                    .filter(s -> s != null)
//                    .sorted(Comparator.comparing(StudentEntity::getUsername))
//                    .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
//                    .toList();
//                subgroupResponses.add(
//                    new SubgroupResponse(sg.getId(), sg.getName(), students)
//                );
//            }
//        }
//
//        var directStudents = useSubgroups
//            ? List.<StudentEntryResponse>of()
//            : request
//                  .studentNames()
//                  .get(0)
//                  .stream()
//                  .map(String::trim)
//                  .filter(u -> !u.isBlank())
//                  .map(existingByUsername::get)
//                  .filter(s -> s != null)
//                  .sorted(Comparator.comparing(StudentEntity::getUsername))
//                  .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
//                  .toList();
//
//        return new StudentGroupResponse(
//            mainGroup.getId(),
//            groupName,
//            directStudents,
//            subgroupResponses
//        );
//    }

//    public StudentGroupResponse findGroupWithSubgroups(UUID id) {
//        var group = groupRepository
//            .findWithSubgroupsAndStudentsById(id)
//            .orElseThrow(() ->
//                new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("User", id))
//            );
//
//        var subgroups = group
//            .getSubgroups()
//            .stream()
//            .sorted(Comparator.comparing(StudentGroupEntity::getName))
//            .map(sg ->
//                new SubgroupResponse(
//                    sg.getId(),
//                    sg.getName(),
//                    mapStudents(sg.getStudents())
//                )
//            )
//            .toList();
//
//        var directStudents = subgroups.isEmpty()
//            ? mapStudents(group.getStudents())
//            : List.<StudentEntryResponse>of();
//
//        return new StudentGroupResponse(
//            group.getId(),
//            group.getName(),
//            directStudents,
//            subgroups
//        );
//    }
//
//    private List<StudentEntryResponse> mapStudents(Collection<StudentEntity> students) {
//        return students
//            .stream()
//            .sorted(Comparator.comparing(StudentEntity::getUsername))
//            .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
//            .toList();
//    }
}
