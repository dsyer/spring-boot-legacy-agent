This project provides a Java agent that allows an app that was compiled with Spring Boot 2.0 to run with earlier versions. The agent adds additional `run` methods to `SpringApplication` and a constructor to `SpringApplicationBuilder` all of which match the signature of the same methods in Spring Boot 2.0 and delegate to the older methods that are actually on the classpath.

The sample "app" is a demo. Notice the call to `LegacyApplication.attach()` in the `main` method (before `SpringApplication` is used):

```java
@SpringBootApplication
public class AgentApplication {

	public static void main(String[] args) {
		LegacyApplication.attach();
		SpringApplication.run(AgentApplication.class, args);
	}

}

```

Example commend line (just replace the main jar with your own):

```
$ mvn package
$ java -jar app/target/spring-boot-legacy-app-0.0.1-SNAPSHOT.jar --thin.profile=old
```

(runs with Spring Boot 1.5.x), and:

```
$ mvn package
$ java -jar target/spring-boot-legacy-agent-0.0.1-SNAPSHOT.jar --thin.profile=snapshot
```

runs with 2.2.x snapshots.

There are, of course, other binary API differences between Spring Boot 1.5 and 2.0, but if you only used the entry points in `SpringApplication` and `SpringApplicationBuilder` the agent will make your app portable.

You can also attach the agent on the command line: `-javaagent:library/target/spring-boot-legacy-agent-0.0.1-SNAPSHOT-agent.jar`. In this case you don't need to call `LegacyApplication.attach()`.
