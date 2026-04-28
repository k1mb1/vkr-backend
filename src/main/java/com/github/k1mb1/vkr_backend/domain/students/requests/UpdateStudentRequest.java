package com.github.k1mb1.vkr_backend.domain.students.requests;

import java.util.UUID;

public record UpdateStudentRequest(String username, UUID groupId) {}
