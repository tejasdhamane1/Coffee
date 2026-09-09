package com.beanbrew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeansAndBrewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BeansAndBrewsApplication.class, args);
    }
}
