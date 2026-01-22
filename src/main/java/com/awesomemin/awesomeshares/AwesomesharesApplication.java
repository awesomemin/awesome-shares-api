package com.awesomemin.awesomeshares;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AwesomesharesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwesomesharesApplication.class, args);
	}

}