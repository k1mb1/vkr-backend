package com.github.k1mb1.vkr_backend.education.structure.api.responses;
import java.util.UUID;
public record GroupPageResponse(UUID id, String name, long subgroupCount) {}
