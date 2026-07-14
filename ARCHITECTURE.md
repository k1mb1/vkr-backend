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
                  ┌──────────────────────────────────┐
                  │             journal              │  журнал: посещаемость,
                  │  (attendance • check-in •        │  check-in, оценки, итоги
                  │   grading • results)             │
                  └──────┬───────────────────────────┘
   lesson::{api,domain,specification}
                  ┌──────▼──────┐
                  │   lesson    │  занятия и проведения (scope)
                  └──────┬──────┘
        subject::{domain,api}, subject (LessonType)
                  ┌──────▼──────┐
                  │   subject   │  предметы, политики, преподаватели, права
                  └──────┬──────┘
             group::{domain,api}
                  ┌──────▼──────┐
                  │    group    │  контингент: группы, подгруппы, студенты
                  └─────────────┘

   auth   — SpEL-бин @authz + снапшот прав; лист: subject/lesson/journal реализуют его SPI (auth::api)
   common — OPEN-модуль: базовые сущности, контракт ошибок, утилиты; лист
```

Направление стрелок = разрешённое направление зависимостей. Обратные рёбра
запрещены явными правилами (`LayerBoundaryRules`) и невозможны по Modulith
(`modules_are_free_of_cycles`, `ModularityTest`).

### Обязанности модулей

| Модуль | Владеет | Публикует |
|---|---|---|
| `group` | группы, подгруппы, **студенты** (один агрегат ростера) | `::domain`; SPI `GroupSubjectsPort` (реализует subject) |
| `subject` | предметы, политики, **справочник преподавателей** и их права, словарь `LessonType` | `::api` (DTO политик), `::domain`, корневые енумы |
| `lesson` | занятия, проведения (scope), видимость под правом | `::api` (`LessonStudentsApi` — ростер DTO; SPI `LessonAssignmentsPort` — реализует journal), `::domain`, `::specification` |
| `journal` | посещаемость, check-in (вложенный пакет `checkin`), задания/оценки, итоги | — (вершина графа; всё потребление — внутри) |
| `auth` | identity из JWT, кэш прав, бин `@authz` для `@PreAuthorize` | `::api` (SPI-порты, реализуются subject/lesson/journal) |
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

Позже для снижения веера зависимостей и дублирования модули **attendance, grading
и results слиты в `journal`** (один домен — сетка «студенты × занятия» с общей
моделью видимости: единый резолвер занятий и общие read-only репозитории вместо
двух копий), а **teacher влит в subject** (право — связь «преподаватель × предмет»,
справочник потреблялся только subject'ом).

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
  сущностями (`Journal*/Lesson*Ref*Repository`) для ссылок и запросов;
  мутации остаются у владельца.
- **Композиция наружу — только DTO** через `::api` (таблицы, ростер, политики) или
  скалярные параметры (id).

## Как проверяется

```bash
./gradlew test --tests 'com.github.k1mb1.vkr_backend.architecture.*'
```

`ModularityTest` дополнительно генерирует PlantUML-диаграммы модулей в
`build/spring-modulith-docs/`.
