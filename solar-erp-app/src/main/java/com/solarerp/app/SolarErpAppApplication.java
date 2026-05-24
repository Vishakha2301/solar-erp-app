package com.solarerp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.solarerp")
@EnableJpaRepositories(basePackages = "com.solarerp")
@EntityScan(basePackages = "com.solarerp")
public class SolarErpAppApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(SolarErpAppApplication.class, args);
	}

}
