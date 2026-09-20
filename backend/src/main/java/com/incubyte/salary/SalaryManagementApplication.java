package com.incubyte.salary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class SalaryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalaryManagementApplication.class, args);
	}

	/**
	 * Injected wherever "today" matters (e.g. current-salary derivation) so
	 * that behavior depending on the current date stays deterministic and
	 * testable via {@link Clock#fixed}.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
