package com.sensorberg.front.resolve

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * spring boot syncApplicationRequest
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
class App {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }
}
