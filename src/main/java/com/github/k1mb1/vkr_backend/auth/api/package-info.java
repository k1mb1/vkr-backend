/**
 * Опубликованные SPI-порты авторизации. Бизнес-модули реализуют их (инверсия
 * зависимостей): subject — {@code PermissionAuthPort}, lesson — {@code LessonAuthPort},
 * attendance — {@code CheckInAuthPort}.
 */
@org.springframework.modulith.NamedInterface("api")
package com.github.k1mb1.vkr_backend.auth.api;
