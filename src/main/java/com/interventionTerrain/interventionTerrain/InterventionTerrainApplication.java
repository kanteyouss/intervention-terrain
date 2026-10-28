package com.interventionTerrain.interventionTerrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InterventionTerrainApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterventionTerrainApplication.class, args);
	}

}
