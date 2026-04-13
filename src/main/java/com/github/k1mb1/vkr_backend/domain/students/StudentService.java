package com.github.k1mb1.vkr_backend.domain.students;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentSubjectSubgroupsResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

	final SubjectRepository subjectRepository;

	public StudentSubjectSubgroupsResponse findBySubjectIdWithSubgroups(
		UUID subjectId
	) {
		var subject = subjectRepository
			.findByIdWithStudentsAndGroups(subjectId)
			.orElseThrow(() ->
				new EntityNotFoundException(
					NOT_FOUND_MESSAGE.formatted("Subject", subjectId)
				)
			);

		var subgroupResponses = subject
			.getStudents()
			.stream()
			.filter(student -> student.getGroup() != null)
			.filter(student -> student.getGroup().getParentGroup() != null)
			.collect(
				java.util.stream.Collectors.groupingBy(
					StudentEntity::getGroup,
					java.util.stream.Collectors.mapping(
						StudentEntity::getUsername,
						java.util.stream.Collectors.toList()
					)
				)
			)
			.entrySet()
			.stream()
			.map(entry ->
				new StudentSubjectSubgroupsResponse.SubjectSubgroupStudentsResponse(
					entry.getKey().getId(),
					entry.getKey().getName(),
					entry
						.getValue()
						.stream()
						.sorted(String::compareTo)
						.toList()
				)
			)
			.sorted(Comparator.comparing(StudentSubjectSubgroupsResponse.SubjectSubgroupStudentsResponse::name))
			.toList();

		return new StudentSubjectSubgroupsResponse(
			subject.getId(),
			subject.getName(),
			subgroupResponses
		);
	}
}
