package com.ens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnsApplication.class, args);
    }

}
