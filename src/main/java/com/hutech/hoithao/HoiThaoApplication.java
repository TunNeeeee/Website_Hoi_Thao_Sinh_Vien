package com.hutech.hoithao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HoiThaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoiThaoApplication.class, args);
    }

}
