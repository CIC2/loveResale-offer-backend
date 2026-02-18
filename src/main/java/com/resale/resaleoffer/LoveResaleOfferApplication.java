package com.resale.resaleoffer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LoveResaleOfferApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoveResaleOfferApplication.class, args);
    }

}


