package com.example.ecommerce_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EcommerceAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceAuthServiceApplication.class, args);
	}

}
