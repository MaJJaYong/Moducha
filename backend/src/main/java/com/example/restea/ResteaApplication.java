package com.example.restea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ResteaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResteaApplication.class, args);
    }

}
