package org.ganesh.keypointhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KeyPointHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyPointHubApplication.class, args);
    }

}
