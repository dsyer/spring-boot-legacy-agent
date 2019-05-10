This project provides a Java agent that allows an app to use `SpringApplication` as normal, but not load the initializers and listeners from `spring.factories`. Instead the user can create a `SpringApplicationCustomizer` and use that to set up the `SpringApplication` explicitly (including listeners and initializers).

The sample "app" is a demo. Notice the call to `SpringApplicationAgent.attach()` in the `main` method (before `SpringApplication` is used):

```java
@SpringBootApplication
@SpringApplicationCustomizers(FactoriesApplicationCustomizer.class)
public class AgentApplication {

	public static void main(String[] args) {
		SpringApplicationAgent.attach();
		SpringApplication.run(AgentApplication.class, args);
	}

}

```

You can also attach the agent on the command line: `-javaagent:agent/target/spring-boot-custom-agent-0.0.1.BUILD-SNAPSHOT.jar`. In this case you don't need to call `SpringApplicationAgent.attach()`.
