package com.hexaware.fastx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FastXApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastXApplication.class, args);
	}

}
