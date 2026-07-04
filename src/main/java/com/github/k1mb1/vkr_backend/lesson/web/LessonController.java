package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.SetActiveLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/lessons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Lessons", description = "Управление занятиями и расписанием")
@RestController
@RequiredArgsConstructor
public class LessonController {

    final LessonApi lessonApi;

    @Operation(summary = "Получить список занятий по permissionId")
    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessons(
            @ParameterObject @Valid @ModelAttribute LessonFilter filter) {
        return ResponseEntity.ok(lessonApi.getLessons(filter));
    }

    @Operation(summary = "Получить занятие по ID")
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(@Parameter(description = "ID занятия") @PathVariable UUID id) {
        return ResponseEntity.ok(lessonApi.getLessonById(id));
    }

    @Operation(
            summary = "Обновить занятие целиком одним запросом",
            description =
                    "В body можно передать любую комбинацию из header / scopes — null = не трогать. Транзакция атомарная.")
    @PutMapping("/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
            @Parameter(description = "ID занятия") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для обновления занятия",
                            required = true)
                    UpdateLessonRequest request) {
        return ResponseEntity.ok(lessonApi.updateLesson(id, request));
    }

    @Operation(
            summary = "Пометить занятие активным (текущим) или снять отметку",
            description =
                    "Активное занятие — точка отсчёта для понижения балла. Не более одного активного на (предмет, тип).")
    @PatchMapping("/{id}/active")
    public ResponseEntity<LessonResponse> setLessonActive(
            @Parameter(description = "ID занятия") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Флаг активности занятия",
                            required = true)
                    SetActiveLessonRequest request) {
        return ResponseEntity.ok(lessonApi.setActive(id, request.active()));
    }

    @Operation(summary = "Удалить занятие")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@Parameter(description = "ID занятия") @PathVariable UUID id) {
        lessonApi.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Массовое создание занятий: N лекций + M практик с allGroups-scope")
    @PostMapping("/bulk")
    public ResponseEntity<List<LessonResponse>> bulkCreateLessons(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Параметры массового создания",
                            required = true)
                    BulkCreateLessonsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonApi.bulkCreate(request));
    }

    @Operation(
            summary = "Создание серии занятий по недельному шаблону",
            description =
                    "Дата первой пары + повторяющийся недельный шаблон (внешний массив — недели, внутренний — дни). "
                            + "Создаёт count занятий: занятие k проводится на k-ю дату каждого item (scope для аудитории item).")
    @PostMapping("/bulk-schedule")
    public ResponseEntity<List<LessonResponse>> bulkScheduleLessons(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Параметры расписания",
                            required = true)
                    BulkScheduleLessonsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonApi.bulkSchedule(request));
    }
}
