package com.buy01.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductApplication {

	public static void main(String[] args) {
		 String dbName = System.getenv("PRODUCT_MONGO_DB_NAME");
        System.out.println("--- DEBUG: Database Name is: [" + dbName + "] ---");
		SpringApplication.run(ProductApplication.class, args);
	}

}
