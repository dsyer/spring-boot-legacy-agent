package org.springframework.boot.legacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AgentApplication {

	public static void main(String[] args) {
		LegacyApplication.attach();
		SpringApplication.run(AgentApplication.class, args);
		SpringApplication.run(new Class<?>[] { AgentApplication.class }, args);
		new SpringApplicationBuilder(AgentApplication.class).run(args);
	}

}
