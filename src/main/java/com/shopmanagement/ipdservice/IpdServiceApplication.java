package com.shopmanagement.ipdservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class IpdServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IpdServiceApplication.class, args);
    }
}
