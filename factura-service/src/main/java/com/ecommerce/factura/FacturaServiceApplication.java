package com.ecommerce.factura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class FacturaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacturaServiceApplication.class, args);
	}

}
