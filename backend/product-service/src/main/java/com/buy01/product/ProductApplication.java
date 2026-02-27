package com.buy01.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductApplication {

	public static void main(String[] args) {
		 String dbName = System.getenv("MONGO_DB_NAME");
        System.out.println("--- DEBUG: Database Name is: [" + dbName + "] ---");
		SpringApplication.run(ProductApplication.class, args);
	}

}
