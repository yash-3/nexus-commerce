package com.nexus.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AiComplianceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiComplianceServiceApplication.class, args);
    }
}
