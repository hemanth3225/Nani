package com.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = "com.claims")  // scans controllers, services
@EnableJpaRepositories(basePackages = "com.claims.repository")  // finds repositories
@EntityScan(basePackages = "com.claims.entity")
public class CostReserveServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CostReserveServiceApplication.class, args);
    }
}
