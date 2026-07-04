package com.github.k1mb1.vkr_backend;

import com.github.k1mb1.vkr_backend.config.NativeImageHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(NativeImageHints.class)
public class VkrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VkrBackendApplication.class, args);
    }
}
// TODO 2026-03-24T23:37:04.728+03:00  WARN 153472 --- [vkr-backend] [nio-4550-exec-9]
// .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.HttpRequestMethodNotSupportedException:
// Request method 'PATCH' is not supported]
