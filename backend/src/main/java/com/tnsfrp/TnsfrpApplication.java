package com.tnsfrp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TnsfrpApplication {
    public static void main(String[] args) {
        SpringApplication.run(TnsfrpApplication.class, args);
    }
}
