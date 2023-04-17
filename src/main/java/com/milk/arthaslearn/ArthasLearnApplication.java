package com.milk.arthaslearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
public class ArthasLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArthasLearnApplication.class, args);
    }

}
