package com.projects.EduConflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EduConfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduConfluxApplication.class, args);
	}
}
