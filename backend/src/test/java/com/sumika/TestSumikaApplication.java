package com.sumika;

import org.springframework.boot.SpringApplication;

public class TestSumikaApplication {

	public static void main(String[] args) {
		SpringApplication.from(SumikaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
