/**
 * Общие технические строительные блоки (базовые сущности, аудит, контракт ошибок,
 * утилиты), переиспользуемые бизнес-модулями. Объявлен OPEN-модулем, чтобы другие
 * модули могли расширять его базовые типы (например {@code BaseEntity}), не
 * спотыкаясь о проверки инкапсуляции Spring Modulith. Лист графа: сам не зависит
 * ни от одного модуля.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.github.k1mb1.vkr_backend.common;
