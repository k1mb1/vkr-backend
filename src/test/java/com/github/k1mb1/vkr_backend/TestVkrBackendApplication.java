package com.github.k1mb1.vkr_backend;

import org.springframework.boot.SpringApplication;

public class TestVkrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(VkrBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
