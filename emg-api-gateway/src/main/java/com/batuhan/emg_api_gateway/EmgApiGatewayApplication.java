package com.batuhan.emg_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@EnableDiscoveryClient
@RefreshScope
@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
public class EmgApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmgApiGatewayApplication.class, args);
	}
}