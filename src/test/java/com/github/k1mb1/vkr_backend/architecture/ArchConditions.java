package com.github.k1mb1.vkr_backend.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.TryCatchBlock;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.validation.Valid;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Reusable {@link ArchCondition}s for rules that ArchUnit's fluent DSL cannot
 * express directly (inspecting generics or annotation attributes). Kept in one
 * place so the rule classes stay declarative.
 */
final class ArchConditions {

    private ArchConditions() {}

    /** A service must depend only on repositories that live in its own feature package. */
    static ArchCondition<JavaClass> dependOnRepositoriesOfOwnPackageOnly() {
        return new ArchCondition<>("depend only on repositories within the same feature package") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                String owner = topPackageOf(clazz.getPackageName());
                for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    boolean isRepository = target.getPackageName().contains(".repository");
                    if (isRepository && !topPackageOf(target.getPackageName()).equals(owner)) {
                        events.add(SimpleConditionEvent.violated(
                                clazz,
                                clazz.getFullName() + " depends on a repository of another package: "
                                        + target.getName() + " (reach foreign data through your own read-only "
                                        + "ref repository or a published port, never its repository)"));
                    }
                }
            }
        };
    }

    /** Every {@code @RequestBody} controller parameter must also be {@code @Valid}. */
    static ArchCondition<JavaMethod> validateEveryRequestBody() {
        return new ArchCondition<>("annotate every @RequestBody parameter with @Valid") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getParameters().forEach(parameter -> {
                    if (parameter.isAnnotatedWith(RequestBody.class) && !parameter.isAnnotatedWith(Valid.class)) {
                        events.add(SimpleConditionEvent.violated(
                                method, method.getFullName() + " has a @RequestBody parameter that is not @Valid"));
                    }
                });
            }
        };
    }

    /** Every {@code @RequestBody} controller parameter must be bound to a {@code *Request} DTO. */
    static ArchCondition<JavaMethod> bindRequestBodyToRequestDto() {
        return new ArchCondition<>("bind every @RequestBody to a *Request DTO") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getParameters().forEach(parameter -> {
                    if (parameter.isAnnotatedWith(RequestBody.class)
                            && !parameter.getRawType().getSimpleName().endsWith("Request")) {
                        events.add(SimpleConditionEvent.violated(
                                method,
                                method.getFullName() + " has a @RequestBody of type "
                                        + parameter.getRawType().getName() + "; bind it to a *Request DTO"));
                    }
                });
            }
        };
    }

    /**
     * Every {@code @PathVariable} / {@code @RequestParam} controller parameter must
     * be a simple value type (a JDK type such as {@code UUID}/{@code String}, a
     * primitive or an enum) — never a DTO or an entity. Path and query parameters
     * carry scalar values (ids, flags); structured input arrives as a {@code @RequestBody}
     * or a bound {@code *Filter} object instead.
     */
    static ArchCondition<JavaMethod> bindPathAndQueryParamsToSimpleValues() {
        return new ArchCondition<>("bind @PathVariable/@RequestParam to simple value types") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getParameters().forEach(parameter -> {
                    boolean isBound = parameter.isAnnotatedWith(PathVariable.class)
                            || parameter.isAnnotatedWith(RequestParam.class);
                    if (isBound && !isSimpleValueType(parameter.getRawType())) {
                        events.add(SimpleConditionEvent.violated(
                                method,
                                method.getFullName() + " binds a @PathVariable/@RequestParam to "
                                        + parameter.getRawType().getName()
                                        + "; path/query parameters must be simple value types (id, flag, ...)"));
                    }
                });
            }
        };
    }

    /** A JDK value type, a primitive or an enum — the only shapes a path/query parameter may take. */
    private static boolean isSimpleValueType(JavaClass type) {
        return type.isPrimitive() || type.isEnum() || type.getPackageName().startsWith("java.");
    }

    /**
     * A controller handler method's name becomes the OpenAPI {@code operationId}, so it must
     * pair the verb of its HTTP method with the resource — the verb is derived from the mapping,
     * not matched against a maintained denylist. GET maps to {@code get*} ({@code getLesson} for one
     * item, {@code getLessons} for a collection/page), POST to {@code create*} ({@code createSubject}), PUT and
     * PATCH to {@code update*} ({@code updateLesson}), DELETE to {@code delete*} ({@code deleteLesson}).
     * A bare verb ({@code get}, {@code list}) fails because it carries no resource. Sub-resource
     * action endpoints — whose method path has a literal segment such as {@code /{id}/return} —
     * are state transitions rather than CRUD, so they only need to be a compound action+resource
     * name ({@code cancelCheckInSession}) without matching the HTTP verb.
     */
    static ArchCondition<JavaMethod> nameEndpointByHttpMethodAndResource() {
        return new ArchCondition<>("be named <verb-of-its-http-method><Resource> (getLesson, createSubject), "
                + "or <action><Resource> for a sub-resource action (cancelCheckInSession)") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                EndpointMapping mapping = mappingOf(method);
                if (mapping == null) {
                    return;
                }
                String name = method.getName();
                boolean carriesResource = name.chars().anyMatch(Character::isUpperCase);
                if (mapping.isAction()) {
                    if (!carriesResource) {
                        events.add(SimpleConditionEvent.violated(
                                method,
                                method.getFullName() + " is an action endpoint named '" + name
                                        + "'; name it <action><Resource> (e.g. cancelCheckInSession)"));
                    }
                } else if (mapping.verbs().stream().noneMatch(verb -> startsWithVerbThenResource(name, verb))) {
                    events.add(SimpleConditionEvent.violated(
                            method,
                            method.getFullName() + " is named '" + name + "'; a " + mapping.verbs()
                                    + "<Resource> name is expected for its HTTP method (e.g. "
                                    + mapping.verbs().iterator().next()
                                    + "Lesson)"));
                }
            }
        };
    }

    /** True when {@code name} is {@code verb} immediately followed by a capitalised resource part. */
    private static boolean startsWithVerbThenResource(String name, String verb) {
        return name.startsWith(verb)
                && name.length() > verb.length()
                && Character.isUpperCase(name.charAt(verb.length()));
    }

    /** The verbs allowed for a handler's HTTP method plus whether it is a sub-resource action. */
    private record EndpointMapping(Set<String> verbs, boolean isAction) {}

    private static @Nullable EndpointMapping mappingOf(JavaMethod method) {
        if (method.isAnnotatedWith(GetMapping.class)) {
            GetMapping m = method.getAnnotationOfType(GetMapping.class);
            // GET is read: get<Resource> for one item, get<Resources> for a collection/page.
            return new EndpointMapping(Set.of("get"), hasLiteralSegment(m.value(), m.path()));
        }
        if (method.isAnnotatedWith(PostMapping.class)) {
            PostMapping m = method.getAnnotationOfType(PostMapping.class);
            return new EndpointMapping(Set.of("create"), hasLiteralSegment(m.value(), m.path()));
        }
        if (method.isAnnotatedWith(PutMapping.class)) {
            PutMapping m = method.getAnnotationOfType(PutMapping.class);
            return new EndpointMapping(Set.of("update"), hasLiteralSegment(m.value(), m.path()));
        }
        if (method.isAnnotatedWith(PatchMapping.class)) {
            PatchMapping m = method.getAnnotationOfType(PatchMapping.class);
            return new EndpointMapping(Set.of("update"), hasLiteralSegment(m.value(), m.path()));
        }
        if (method.isAnnotatedWith(DeleteMapping.class)) {
            DeleteMapping m = method.getAnnotationOfType(DeleteMapping.class);
            return new EndpointMapping(Set.of("delete"), hasLiteralSegment(m.value(), m.path()));
        }
        return null;
    }

    /** A method-level path with a literal (non-{@code {var}}) segment marks a sub-resource action. */
    @SafeVarargs
    private static boolean hasLiteralSegment(String[]... pathGroups) {
        for (String[] paths : pathGroups) {
            for (String path : paths) {
                for (String segment : path.split("/")) {
                    if (!segment.isBlank() && !segment.startsWith("{")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * A complex query-bound controller parameter must be documented as expanded query
     * parameters for OpenAPI: a {@code *Filter} carries {@code @ParameterObject} +
     * {@code @ModelAttribute}, and a {@code Pageable} carries {@code @ParameterObject}.
     * Without {@code @ParameterObject} springdoc renders the object as a single opaque
     * body/param instead of its individual fields.
     */
    static ArchCondition<JavaMethod> documentBoundFilterAndPageable() {
        return new ArchCondition<>(
                "annotate a bound *Filter with @ParameterObject + @ModelAttribute and a Pageable with @ParameterObject") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getParameters().forEach(parameter -> {
                    JavaClass type = parameter.getRawType();
                    if (type.getSimpleName().endsWith("Filter")
                            && !(parameter.isAnnotatedWith(ParameterObject.class)
                                    && parameter.isAnnotatedWith(ModelAttribute.class))) {
                        events.add(SimpleConditionEvent.violated(
                                method,
                                method.getFullName() + " binds a *Filter (" + type.getName()
                                        + ") that is not @ParameterObject + @ModelAttribute"));
                    } else if (type.isEquivalentTo(Pageable.class)
                            && !parameter.isAnnotatedWith(ParameterObject.class)) {
                        events.add(SimpleConditionEvent.violated(
                                method, method.getFullName() + " binds a Pageable that is not @ParameterObject"));
                    }
                });
            }
        };
    }

    /** A {@code @Service} must carry a class-level {@code @Transactional(readOnly = true)}. */
    static ArchCondition<JavaClass> beAnnotatedWithReadOnlyTransactional() {
        return new ArchCondition<>("be annotated @Transactional(readOnly = true) at class level") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean readOnly = clazz.isAnnotatedWith(Transactional.class)
                        && clazz.getAnnotationOfType(Transactional.class).readOnly();
                if (!readOnly) {
                    events.add(SimpleConditionEvent.violated(
                            clazz, clazz.getName() + " must be annotated @Transactional(readOnly = true)"));
                }
            }
        };
    }

    /**
     * A Spring stereotype must expose exactly one constructor, so the framework
     * injects collaborators unambiguously without {@code @Autowired}.
     */
    static ArchCondition<JavaClass> haveExactlyOneConstructor() {
        return new ArchCondition<>("have exactly one constructor (unambiguous constructor injection)") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                int constructors = clazz.getConstructors().size();
                if (constructors != 1) {
                    events.add(SimpleConditionEvent.violated(
                            clazz,
                            clazz.getName() + " has " + constructors
                                    + " constructors; expected exactly one for constructor injection"));
                }
            }
        };
    }

    /**
     * A public service method must return a {@code *Response} DTO (optionally wrapped
     * in a generic container such as {@code List<...Response>}) or {@code void}, and
     * must never accept a {@code *Entity} type as a parameter (at any generic depth).
     */
    static ArchCondition<JavaMethod> exposeOnlyDtosOrVoid() {
        return new ArchCondition<>("return a *Response DTO (or void) and never take a *Entity parameter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                checkReturnType(method.getReturnType(), method, events);
                method.getParameterTypes().forEach(type -> checkParameterType(type, method, events));
            }
        };
    }

    /**
     * A class must not catch the generic {@code Exception} or {@code Throwable} — catch the
     * specific type instead, so the failure you did not anticipate is not silently swallowed.
     */
    static ArchCondition<JavaClass> notCatchGenericExceptions() {
        return new ArchCondition<>("not catch generic Exception or Throwable") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                clazz.getCodeUnits().forEach(codeUnit -> checkCaughtTypes(codeUnit, events));
            }
        };
    }

    private static void checkCaughtTypes(JavaCodeUnit codeUnit, ConditionEvents events) {
        for (TryCatchBlock block : codeUnit.getTryCatchBlocks()) {
            for (JavaClass caught : block.getCaughtThrowables()) {
                if (isGenericThrowable(caught)) {
                    events.add(SimpleConditionEvent.violated(
                            codeUnit, codeUnit.getFullName() + " catches " + caught.getName()));
                }
            }
        }
    }

    private static boolean isGenericThrowable(JavaClass caught) {
        return caught.isEquivalentTo(Exception.class) || caught.isEquivalentTo(Throwable.class);
    }

    /**
     * A MapStruct {@code @Mapper} must declare {@code componentModel = "spring"} so the
     * generated implementation is a Spring bean that services inject by constructor,
     * rather than a default mapper obtained through {@code Mappers.getMapper(...)}.
     */
    static ArchCondition<JavaClass> useSpringComponentModel() {
        return new ArchCondition<>("declare @Mapper(componentModel = \"spring\")") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                String componentModel = clazz.getAnnotationOfType(Mapper.class).componentModel();
                if (!"spring".equals(componentModel)) {
                    events.add(SimpleConditionEvent.violated(
                            clazz,
                            clazz.getName() + " uses componentModel=\"" + componentModel
                                    + "\"; mappers must be Spring beans (componentModel = \"spring\")"));
                }
            }
        };
    }

    /** A method must not take {@link Optional} as a parameter ({@code Optional} is a return type only). */
    static ArchCondition<JavaMethod> notUseOptionalAsParameter() {
        return new ArchCondition<>("not take an Optional parameter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getParameterTypes().forEach(type -> {
                    if (type.toErasure().isEquivalentTo(Optional.class)) {
                        events.add(SimpleConditionEvent.violated(
                                method, method.getFullName() + " takes an Optional parameter"));
                    }
                });
            }
        };
    }

    private static void checkReturnType(JavaType type, JavaMethod method, ConditionEvents events) {
        checkReturnType(type, method, events, true);
    }

    private static void checkReturnType(JavaType type, JavaMethod method, ConditionEvents events, boolean topLevel) {
        // Unwrap generic containers (List<...>, Optional<...>, Map<...>, ...) and check the arguments.
        if (type instanceof JavaParameterizedType parameterized) {
            parameterized.getActualTypeArguments().forEach(arg -> checkReturnType(arg, method, events, false));
            return;
        }
        JavaClass erasure = type.toErasure();
        String name = erasure.getName();
        if ("void".equals(name)) {
            return;
        }
        // Inside a generic (e.g. the UUID key of Map<UUID, GradeCellResponse>) plain JDK value
        // types are fine; only the payload type must be a *Response. At the top level we
        // still require a *Response DTO.
        if (!topLevel && (name.startsWith("java.") || erasure.isPrimitive())) {
            return;
        }
        if (!erasure.getSimpleName().endsWith("Response")) {
            events.add(SimpleConditionEvent.violated(
                    method,
                    method.getFullName() + " returns " + name
                            + " — service methods must return a *Response DTO or void"));
        }
    }

    private static void checkParameterType(JavaType type, JavaMethod method, ConditionEvents events) {
        JavaClass erasure = type.toErasure();
        if (erasure.getSimpleName().endsWith("Entity")) {
            events.add(SimpleConditionEvent.violated(
                    method, method.getFullName() + " takes a *Entity parameter: " + erasure.getName()));
        }
        if (type instanceof JavaParameterizedType parameterized) {
            parameterized.getActualTypeArguments().forEach(arg -> checkParameterType(arg, method, events));
        }
    }

    /**
     * A {@code *Repository} declared over another feature package's entity is a read-only
     * view: it may expose finders and {@code getReferenceById}, but never writes. Concretely
     * it must extend the Spring Data {@code Repository} marker (plus optionally
     * {@code JpaSpecificationExecutor}) and must not be assignable to {@code CrudRepository},
     * which would drag in {@code save}/{@code delete} and let a consumer mutate an aggregate
     * it does not own. A repository over its own package's entity is unrestricted.
     */
    static ArchCondition<JavaClass> keepForeignEntityRepositoriesReadOnly() {
        return new ArchCondition<>(
                "be read-only (extend Repository, not CrudRepository) over another package's entity") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                Class<?> repo = clazz.reflect();
                Class<?> entity = managedEntityOf(repo);
                if (entity == null) {
                    return; // not a Spring Data repository, or a non-class type argument
                }
                String repoPackage = topPackageOf(clazz.getPackageName());
                String entityPackage = topPackageOf(entity.getPackageName());
                boolean foreign = !entityPackage.isEmpty() && !entityPackage.equals(repoPackage);
                if (foreign && CrudRepository.class.isAssignableFrom(repo)) {
                    events.add(SimpleConditionEvent.violated(
                            clazz,
                            clazz.getName() + " is a CrudRepository over " + entity.getName()
                                    + " owned by package '" + entityPackage + "'; a cross-package repository must be a "
                                    + "read-only view (extend Repository, never CrudRepository/JpaRepository)"));
                }
            }
        };
    }

    /** The entity type a Spring Data repository interface manages (first arg of its {@code Repository<T, ID>} super). */
    private static @Nullable Class<?> managedEntityOf(Class<?> repo) {
        for (Type generic : repo.getGenericInterfaces()) {
            if (generic instanceof ParameterizedType parameterized
                    && parameterized.getRawType() instanceof Class<?> raw
                    && Repository.class.isAssignableFrom(raw)
                    && parameterized.getActualTypeArguments()[0] instanceof Class<?> entity) {
                return entity;
            }
        }
        return null;
    }

    /** Top-level feature-package segment of a package name, e.g. {@code journal} or {@code teacher}. */
    private static String topPackageOf(String packageName) {
        String base = Packages.ROOT + ".";
        if (!packageName.startsWith(base)) {
            return "";
        }
        String rest = packageName.substring(base.length());
        int dot = rest.indexOf('.');
        return dot < 0 ? rest : rest.substring(0, dot);
    }
}
