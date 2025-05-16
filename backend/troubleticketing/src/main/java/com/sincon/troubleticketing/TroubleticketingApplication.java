package com.sincon.troubleticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TroubleticketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TroubleticketingApplication.class, args);
	}

}
