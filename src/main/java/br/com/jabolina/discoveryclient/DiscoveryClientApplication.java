package br.com.jabolina.discoveryclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class DiscoveryClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryClientApplication.class, args);
	}

}
