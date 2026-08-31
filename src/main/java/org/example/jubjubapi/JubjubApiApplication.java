package org.example.jubjubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JubjubApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JubjubApiApplication.class, args);
    }

}
