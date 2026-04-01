package com.github.k1mb1.vkr_backend.domain.students;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    final StudentRepository studentRepository;
    final StudentMapper studentMapper;
}
