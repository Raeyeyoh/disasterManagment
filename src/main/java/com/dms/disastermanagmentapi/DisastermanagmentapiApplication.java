package com.dms.disastermanagmentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAsync
@SpringBootApplication
public class DisastermanagmentapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DisastermanagmentapiApplication.class, args);
	}

}
