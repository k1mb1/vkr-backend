package com.github.k1mb1.vkr_backend.lesson;

import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LessonApi {

    LessonResponse update(UUID id, UpdateLessonRequest request);

    void delete(UUID id);

    Page<LessonResponse> getPage(LessonFilter filter, Pageable pageable);

    List<LessonResponse> bulkSchedule(BulkScheduleRequest request);

    List<LessonResponse> createByType(CreateLessonsByTypeRequest request);
}
