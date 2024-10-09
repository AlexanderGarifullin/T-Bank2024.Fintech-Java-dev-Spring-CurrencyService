package com.example.currencies;

import org.springframework.boot.SpringApplication;

public class TestCurrenciesApplication {

	public static void main(String[] args) {
		SpringApplication.from(CurrenciesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
