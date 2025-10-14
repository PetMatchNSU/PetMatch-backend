package org.nsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "org.nsu")
@EnableJpaRepositories(basePackages = "org.nsu")
public class PetMatch {

    public static void main(String[] args) {
        SpringApplication.run(PetMatch.class, args);
    }

}