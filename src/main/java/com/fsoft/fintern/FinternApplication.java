package com.fsoft.fintern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class FinternApplication {
	public static void main(String[] args) {
		System.setProperty("java.io.tmpdir", "C:/Temp");
		SpringApplication.run(FinternApplication.class, args);
	}


}
