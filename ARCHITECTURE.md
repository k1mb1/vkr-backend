# Архитектура

Сервис — **единый Spring Boot монолит** с одной схемой БД. Код разложен по
доменным пакетам, а внутри каждого пакета — по слоям. Слоевые конвенции — не
пожелания: они закреплены исполняемыми тестами (`src/test/.../architecture/*` на
ArchUnit) и падают в CI при нарушении.

## Доменные пакеты

Каждый top-level пакет под `com.github.k1mb1.vkr_backend` — самостоятельная
предметная область. Пакеты — обычные Java-пакеты: они видят публичные типы друг
друга напрямую, отдельного механизма изоляции модулей нет.

| Пакет | Отвечает за |
|---|---|
| `group` | группы, подгруппы, **студенты** (один агрегат ростера) |
| `subject` | предметы, политики, словарь `LessonType` |
| `teacher` | **справочник преподавателей** и выданные им права (право — связь «преподаватель × предмет» со scope'ами) |
| `lesson` | занятия, проведения (scope), видимость под правом |
| `journal` | посещаемость, check-in (вложенный пакет `checkin`), задания/оценки, итоги |
| `auth` | identity из JWT, кэш прав, бин `@authz` для `@PreAuthorize` |
| `common` | `BaseEntity`/`Auditable`, контракт ошибок (`ErrorDto`, handler), утилиты |

Границы пакетов проведены по агрегатам: корень + сущности, которые без него не
существуют (`group` = группа+подгруппа+студент; `subject` = предмет+политики;
`teacher` = преподаватель+права+scope'ы; `journal/checkin` = сессия+отметки).

Направление зависимостей выдержано сверху вниз (журнал строится над занятиями,
занятия — над предметами и правами):

```
journal → lesson → subject → group
             └──→ teacher → subject, group   (право ссылается на предмет и аудиторию)
journal, lesson → teacher                    (видимость таблиц под правом)
auth    → (только common); авторизацию вызывают все доменные пакеты
common  → (лист; от него зависят все, он — ни от кого)
```

Историческая консолидация: attendance/grading/results слиты в `journal` (один
домен — сетка «студенты × занятия» с общей моделью видимости).

Циклы разорваны инверсией через порты — доменный пакет объявляет интерфейс,
реализует его владелец данных (обычные Spring-бины):

- `group.api.GroupSubjectsPort` — реализует subject;
- `lesson.api.LessonAssignmentsPort` — реализует journal;
- `auth.api.PermissionAuthPort` — реализует teacher;
- **`subject.api.OwnerPermissionGranter` / `SubjectVisibilityPort`** — реализует
  teacher: чтобы subject не зависел от teacher, авто-грант права владельцу при
  создании предмета и фильтр «свои предметы» вынесены за порты (subject отдаёт
  скалярные id, а видимые предметы получает готовым набором).

## Слои внутри пакета

```
<пакет>/controller       REST-контроллеры (@RestController, ResponseEntity)
<пакет>/service          use case'ы (@Service, class-level @Transactional(readOnly=true))
<пакет>/service/dto      records: request/ | response/ | filter/
<пакет>/mapper           MapStruct @Mapper(componentModel="spring")
<пакет>/specification    JPA Specification-билдеры (*Specification(s))
<пакет>/repository       Spring Data интерфейсы *Repository
<пакет>/domain           JPA-сущности *Entity (наследуют Auditable/BaseEntity)
<пакет>/api              порты и DTO-records для обмена между пакетами
<пакет>/<корень>         package-info + общий словарь (енумы)
```

Направление зависимостей: controller → service → {mapper, specification,
repository} → domain; domain не зависит ни от какого слоя (кроме словаря в корне
пакета). Транспорт (DTO) не знает про JPA; сущности не утекают из сервисов
(`services_expose_only_dtos_and_never_take_entities`).

## Данные между пакетами

- **JPA-связи между пакетами разрешены** (это монолит с одной схемой) — на
  доменные `*.domain` типы.
- **Композиция наружу — через DTO** из `*.api` (таблицы, ростер, политики) или
  скалярные параметры (id).

## Как проверяется

```bash
./gradlew test --tests 'com.github.k1mb1.vkr_backend.architecture.*'
```

ArchUnit-правила закрепляют:

- **слоение** (`LayeredArchitectureRules`, `LayerBoundaryRules`): контроллеры не
  трогают persistence, DTO не зависят от сущностей, домен framework-agnostic;
- **конвенции слоёв** (`NamingConventionRules`, `MapperRules`,
  `SpecificationRules`, `TransactionRules`, `WebContractRules`,
  `AuthorizationRules`, `PersistenceAndContractRules`, `MaintainabilityRules`,
  `CodingHygieneRules`).
