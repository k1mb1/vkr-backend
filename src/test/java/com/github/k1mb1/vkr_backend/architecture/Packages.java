package com.github.k1mb1.vkr_backend.architecture;

/**
 * Single source of truth for the package conventions the architecture tests rely
 * on. Centralising the patterns here means a layer is renamed in exactly one
 * place, and a typo can no longer turn a rule silently green.
 */
final class Packages {

    /** Application root package. */
    static final String ROOT = "com.github.k1mb1.vkr_backend";

    // In-module layers (matched anywhere in the package tree, across all modules).
    static final String CONTROLLER = "..controller..";
    static final String SERVICE = "..service..";
    static final String DTO = "..service.dto..";
    // DTOs are split by direction: inbound *Request, outbound *Response and *Filter
    // (list/query criteria). Each sub-package holds exactly one kind.
    static final String DTO_REQUEST = "..service.dto.request..";
    static final String DTO_RESPONSE = "..service.dto.response..";
    static final String DTO_FILTER = "..service.dto.filter..";
    static final String MAPPER = "..mapper..";
    static final String SPECIFICATION = "..specification..";
    // Scoped to our own packages on purpose: a bare "..domain.." would also match
    // Spring Data's org.springframework.data.domain (Page/Pageable) and turn a
    // legitimate pagination dependency into a false "touches persistence" violation.
    static final String DOMAIN = ROOT + "..domain..";
    static final String REPOSITORY = "..repository..";
    static final String CONFIG = "..config..";
    static final String EXCEPTION = "..exception..";

    /** Published api packages (ports and DTO records shared between feature packages). */
    static final String API = "..api..";

    // Feature (aggregate) packages in dependency order: each may depend only on the
    // ones before it. The graph rules reference these constants so a package rename
    // cannot silently detach a rule. See ARCHITECTURE.md.
    static final String PKG_GROUP = ROOT + ".group..";
    static final String PKG_SUBJECT = ROOT + ".subject..";
    static final String PKG_TEACHER = ROOT + ".teacher..";
    static final String PKG_LESSON = ROOT + ".lesson..";
    static final String PKG_JOURNAL = ROOT + ".journal..";
    static final String PKG_AUTH = ROOT + ".auth..";

    /** All business feature packages (everything except the auth/common leaves). */
    static final String[] BUSINESS = {PKG_GROUP, PKG_SUBJECT, PKG_TEACHER, PKG_LESSON, PKG_JOURNAL};

    /** Business packages plus auth — nothing here may be depended on by the common leaf. */
    static final String[] BUSINESS_AND_AUTH = {PKG_GROUP, PKG_SUBJECT, PKG_TEACHER, PKG_LESSON, PKG_JOURNAL, PKG_AUTH};

    /** Shared technical package. */
    static final String COMMON = "..common..";

    // External framework packages we reason about by name.
    static final String JPA = "jakarta.persistence..";
    static final String SPRING = "org.springframework..";
    static final String SPRING_HTTP = "org.springframework.http..";
    static final String SERVLET = "jakarta.servlet..";

    private Packages() {}
}
