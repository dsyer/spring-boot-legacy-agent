package org.springframework.boot.sample;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.agent.SpringApplicationAgent;
import org.springframework.boot.agent.SpringApplicationCustomizer;
import org.springframework.boot.agent.SpringApplicationCustomizers;
import org.springframework.boot.autoconfigure.BackgroundPreinitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.logging.LoggingApplicationListener;

@SpringBootApplication(proxyBeanMethods = false)
@SpringApplicationCustomizers(FactoriesApplicationCustomizer.class)
public class AgentApplication {

	public static void main(String[] args) throws Exception {
		SpringApplicationAgent.install();
		SpringApplication.run(AgentApplication.class, args);
	}

}

class FactoriesApplicationCustomizer implements SpringApplicationCustomizer {

	@Override
	public void customize(SpringApplication application, String[] args) {
		System.err.println("Customizing SpringApplication");
		application.setListeners(Arrays.asList(new BackgroundPreinitializer(),
				new ConfigFileApplicationListener(), new LoggingApplicationListener()));
		application.setInitializers(
				Arrays.asList(new ContextIdApplicationContextInitializer()));
	}

}