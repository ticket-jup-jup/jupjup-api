package org.example.jubjubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling//polling 스케쥴링
public class JubjubApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JubjubApiApplication.class, args);
    }
}
