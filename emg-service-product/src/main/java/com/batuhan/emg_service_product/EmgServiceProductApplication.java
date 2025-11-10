package com.batuhan.emg_service_product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EmgServiceProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmgServiceProductApplication.class, args);
	}
}