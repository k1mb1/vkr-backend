# Архитектура

Сервис построен как **модульный монолит** на Spring Modulith. Правила ниже — не
пожелания: они закреплены исполняемыми тестами (`src/test/.../architecture/*` на
ArchUnit + `ModularityTest` на Spring Modulith) и падают в CI при нарушении.

## Модули и сферы влияния

Каждый top-level пакет под `com.github.k1mb1.vkr_backend` — модуль Spring Modulith.
Его границы и разрешённые зависимости объявлены в `package-info.java`
(`@ApplicationModule(allowedDependencies = ...)`), опубликованные поверхности — в
named interfaces (`@NamedInterface`).

```
                        ┌─────────┐
                        │ results │  итоги семестра (read-only агрегатор)
                        └────┬────┘
              grading::api ──┤── attendance::api
                        ┌────▼────┐
                        │ grading │  задания и оценки
                        └────┬────┘
            attendance::api ─┤
                        ┌────▼──────┐
                        │attendance │  посещаемость (+ вложенный check-in)
                        └────┬──────┘
   lesson::{api,domain,spec}─┤
                        ┌────▼────┐
                        │ lesson  │  занятия и проведения (scope)
                        └────┬────┘
      subject::{domain,api} ─┤
                        ┌────▼────┐
                        │ subject │  предметы, политики, права (владеет LessonType)
                        └──┬───┬──┘
        group::{domain,api}│   │teacher::domain
                    ┌──────▼┐ ┌▼────────┐
                    │ group │ │ teacher │   справочники (низ графа)
                    └───────┘ └─────────┘

   auth   — SpEL-бин @authz + снапшот прав; лист: бизнес-модули реализуют его SPI (auth::api)
   common — OPEN-модуль: базовые сущности, контракт ошибок, утилиты; лист
```

Направление стрелок = разрешённое направление зависимостей. Обратные рёбра
запрещены явными правилами (`LayerBoundaryRules`) и невозможны по Modulith
(`modules_are_free_of_cycles`, `ModularityTest`).

### Обязанности модулей

| Модуль | Владеет | Публикует |
|---|---|---|
| `teacher` | справочник преподавателей (id = `sub` из Keycloak) | `::domain` (FK для subject) |
| `group` | группы, подгруппы, **студенты** (один агрегат ростера) | `::domain`; SPI `GroupSubjectsPort` (реализует subject) |
| `subject` | предметы, политики, права преподавателей, словарь `LessonType` | `::api` (DTO политик), `::domain`, корневые енумы |
| `lesson` | занятия, проведения (scope), видимость под правом | `::api` (`LessonStudentsApi` — ростер DTO; SPI `LessonAssignmentsPort` — реализует grading), `::domain`, `::specification` |
| `attendance` | отметки посещаемости; подмодуль `checkin` (QR-самоотметка) | `::api` (таблица, сводка) |
| `grading` | задания и оценки, таблица с политиками и вкладом посещаемости | `::api` (таблица) |
| `results` | композиция «оценки + посещаемость» одним запросом | — (вершина графа) |
| `auth` | identity из JWT, кэш прав, бин `@authz` для `@PreAuthorize` | `::api` (SPI-порты, реализуются subject/lesson/attendance) |
| `common` | `BaseEntity`/`Auditable`, контракт ошибок (`ErrorDto`, handler), утилиты | всё (OPEN) |

### Как разорваны исторические циклы

- **group ↔ student** — student влит в group: ростер и группы — один агрегат.
- **subject ↔ lesson** — `LessonType` переехал в subject (словарь политик);
  lesson зависит от subject, не наоборот.
- **lesson ↔ grading** — инверсия: lesson объявляет SPI `LessonAssignmentsPort`
  в своём `api`, grading его реализует.
- **group ↔ subject** — связью «предмет—группы» владеет subject; group зовёт SPI
  `GroupSubjectsPort`, не зная о subject.
- **common → бизнес-модули** — авторизация вынесена в `auth`; данные для проверок
  приходят через SPI `auth::api`, реализуемые владельцами данных.

## Слои внутри модуля

```
<module>/controller       REST-контроллеры (@RestController, ResponseEntity)
<module>/service          use case'ы (@Service, class-level @Transactional(readOnly=true))
<module>/service/dto      records: request/ | response/ | filter/
<module>/mapper           MapStruct @Mapper(componentModel="spring")
<module>/specification    JPA Specification-билдеры (*Specification(s))
<module>/repository       Spring Data интерфейсы *Repository
<module>/domain           JPA-сущности *Entity (наследуют Auditable/BaseEntity)
<module>/api              опубликованные порты и DTO-records (@NamedInterface)
<module>/<корень>         package-info + общий словарь (енумы), unnamed interface
```

Направление зависимостей: controller → service → {mapper, specification,
repository} → domain; domain не зависит ни от какого слоя (кроме словаря в корне
модуля). Транспорт (DTO) не знает про JPA; сущности не утекают из сервисов
(`services_expose_only_dtos_and_never_take_entities`).

## Межмодульные данные

- **JPA-связи между модулями разрешены** (это монолит с одной схемой), но только
  на опубликованные `::domain` типы, и каждая такая связь видна в
  `allowedDependencies` модуля-потребителя.
- **Чужими репозиториями пользоваться нельзя** (`services_use_only_their_own_modules_repositories`):
  модуль объявляет собственные (обычно read-only) репозитории над чужими
  сущностями (`Attendance*/Grading*/Lesson*Ref*Repository`) для ссылок и запросов;
  мутации остаются у владельца.
- **Композиция наружу — только DTO** через `::api` (таблицы, ростер, политики) или
  скалярные параметры (id).

## Как проверяется

```bash
./gradlew test --tests 'com.github.k1mb1.vkr_backend.architecture.*'
```

`ModularityTest` дополнительно генерирует PlantUML-диаграммы модулей в
`build/spring-modulith-docs/`.
