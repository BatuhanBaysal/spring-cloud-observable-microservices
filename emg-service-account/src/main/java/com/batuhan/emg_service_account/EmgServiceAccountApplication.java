package com.batuhan.emg_service_account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@EnableDiscoveryClient
@RefreshScope
@SpringBootApplication
public class EmgServiceAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmgServiceAccountApplication.class, args);
	}
}