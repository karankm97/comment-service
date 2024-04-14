package com.km.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CommentserviceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CommentserviceApplication.class, args);
	}
}
