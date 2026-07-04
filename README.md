# VKR Backend — API системы учёта учебного процесса

REST API для системы учёта учебного процесса: группы и студенты, предметы и
доступы преподавателей, занятия и задания, отметка присутствия по QR-коду,
журнал оценок, гибкие политики оценивания и автоматическая промежуточная
аттестация.

Сервис на **Spring Boot 4** (Java 25). Stateless-аутентификация через OAuth2
(JWT, resource server). Это бэкенд для фронтенда
[VKR](https://github.com/k1mb1/vkr) на Nuxt; браузер не обращается к API
напрямую — запросы идут через серверный прокси Nuxt с подстановкой токена.

## Возможности

- **Группы и подгруппы** — управление группами, подгруппами и составом студентов.
- **Предметы и доступы** — назначение преподавателей с полным доступом или
  ограничением по группам, подгруппам и типу занятий (лекции/практики)
  (`teacher-subject-permissions`).
- **Занятия** — лекции и практики, проведения по группам/подгруппам с областями
  видимости (`lesson-scopes`), задания с режимами допуска.
- **Отметка присутствия (check-in)** — сессии check-in по занятию, генерация
  кода аудитории и QR, публичный эндпоинт для студента (без аутентификации),
  подтверждение результатов преподавателем и перенос в посещаемость.
- **Посещаемость и оценки** — сводные таблицы, массовое выставление,
  агрегированные итоги (`results`).
- **Политики оценивания** (на уровне предмета):
  - штрафы/бонусы за сроки сдачи (`penalty-policy`);
  - учёт посещаемости в баллах (`attendance-policy`);
  - подсветка таблиц оценок и посещаемости (`grading-highlight-policy`,
    `attendance-highlight-policy`);
  - параметры check-in (`check-in-policy`);
  - промежуточная аттестация — уровни по баллам и обязательным задачам с порогом
    по посещаемости (`final-assessment-policy`).
- **OpenAPI/Swagger** — генерация схемы (`/v3/api-docs`) и UI (`/swagger-ui.html`)
  в dev-профиле; схема используется фронтендом как источник типов клиента.

## Технологии

- [Spring Boot 4](https://spring.io/projects/spring-boot) + Spring Web MVC, Java 25
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) + Hibernate, PostgreSQL
- [Spring Security](https://spring.io/projects/spring-security) — OAuth2 Resource
  Server (JWT), stateless-сессии
- [Liquibase](https://www.liquibase.org/) — миграции БД
- [MapStruct](https://mapstruct.org/) — маппинг DTO ↔ сущности
- [Lombok](https://projectlombok.org/)
- [springdoc-openapi](https://springdoc.org/) — OpenAPI 3 / Swagger UI
- [Testcontainers](https://testcontainers.com/) — интеграционные тесты на PostgreSQL
- [GraalVM Native Image](https://www.graalvm.org/) — нативная сборка (профиль `native`)

## Архитектура

```
Браузер ──► Nuxt (прокси /api/proxy/**) ──► VKR Backend API (этот проект)
   (cookie-сессия)      (подстановка JWT)        ├─ OAuth2 Resource Server (JWT)
                                                  ├─ Spring MVC контроллеры (/api/**)
                                                  ├─ JPA / Hibernate
                                                  └─ PostgreSQL (миграции Liquibase)
```

Код организован по доменным модулям (`attendance`, `grading`, `group`, `lesson`,
`results`, `student`, `subject`, `teacher`). В каждом модуле выделены:

- `web/` — контроллеры, запросы/ответы (DTO), фильтры;
- `domain/` — JPA-сущности и доменные перечисления;
- `internal/` — сервисы, репозитории, мапперы (детали реализации);
- публичные `*Api` / `*Service` интерфейсы — точки взаимодействия между модулями.

## Требования

- JDK 25
- Gradle (можно использовать встроенный `./gradlew`)
- PostgreSQL 17 (или `docker compose`)
- OAuth2/OIDC-провайдер (issuer для проверки JWT)

## Установка

```bash
cp .env.example .env
```

Заполните `.env`:

```bash
# Issuer OIDC-провайдера для проверки JWT
OAUTH2_RESOURSESERVER_ISSUER_URI=https://id.example.com/realms/vkr

# Разрешённые origin для CORS (через запятую)
CORS_ALLOWED_ORIGINS=http://front.localhost

# Подключение к БД
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# Профиль (dev — Swagger UI и подробные логи; prod — по умолчанию)
SPRING_PROFILES_ACTIVE=dev
```

Поднять PostgreSQL для локальной разработки:

```bash
docker compose up -d db-postgres
```

## Запуск

```bash
./gradlew bootRun               # запуск приложения
```

API поднимется на `http://localhost:8080`. В профиле `dev` доступны:

- Swagger UI — `http://localhost:8080/swagger-ui.html`
- OpenAPI-схема — `http://localhost:8080/v3/api-docs`

Миграции БД применяются автоматически при старте (Liquibase).

## Сборка

```bash
./gradlew clean build           # JAR в build/libs/
./gradlew test                  # тесты (Testcontainers поднимает PostgreSQL)
```

### Docker

```bash
docker build -t vkr-backend .                 # JVM-образ
docker build -f Dockerfile.native -t vkr-backend:native .   # GraalVM native
```

### Нативная сборка (GraalVM)

```bash
./gradlew nativeCompile
```

## Профили

| Профиль | Назначение                                                              |
| ------- | ---------------------------------------------------------------------- |
| `prod`  | По умолчанию. Swagger выключен, минимум логов, сжатие ответов          |
| `dev`   | Swagger UI, подробное логирование SQL и запросов                       |
| `test`  | Интеграционные тесты на Testcontainers (PostgreSQL)                    |

## Переменные окружения

| Переменная                          | Обязательна | Описание                                          |
| ----------------------------------- | ----------- | ------------------------------------------------- |
| `SPRING_DATASOURCE_URL`             | да          | JDBC URL PostgreSQL                               |
| `SPRING_DATASOURCE_USERNAME`        | да          | Пользователь БД                                   |
| `SPRING_DATASOURCE_PASSWORD`        | да          | Пароль БД                                         |
| `OAUTH2_RESOURSESERVER_ISSUER_URI`  | да          | Issuer URI OIDC-провайдера (проверка JWT)         |
| `CORS_ALLOWED_ORIGINS`              | да          | Разрешённые origin для CORS (через запятую)       |
| `SPRING_PROFILES_ACTIVE`            | нет         | Активный профиль (`dev`); по умолчанию `prod`     |
| `PORT`                              | нет         | Порт сервера (по умолчанию `8080`)                |

## Структура проекта

```
src/main/java/.../vkr_backend/
  attendance/        Посещаемость и check-in (сессии, записи, QR-коды)
  grading/           Задания и оценки
  group/             Группы, подгруппы, студенты в группах
  lesson/            Занятия (лекции/практики), проведения, области видимости
  results/           Сводные итоги по студентам
  student/           Студенты
  subject/           Предметы, доступы преподавателей, политики оценивания
  teacher/           Преподаватели
  common/            Общие утилиты
  config/, configs/  Security (OAuth2 JWT, CORS), OpenAPI, JPA-аудит, native hints
src/main/resources/
  application*.yaml   Конфигурация по профилям
  db/changelog/       Миграции Liquibase
```

## Безопасность

- Stateless OAuth2 Resource Server: каждый запрос аутентифицируется по JWT
  (`Authorization: Bearer`).
- Все эндпоинты требуют аутентификации, кроме Swagger и публичной страницы
  check-in (`/api/check-in-sessions/public/**`), защищённой кодом аудитории.
- CORS настраивается через `CORS_ALLOWED_ORIGINS`, разрешена передача
  учётных данных.
