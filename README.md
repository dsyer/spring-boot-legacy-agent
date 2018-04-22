This project provides a Java agent that allows an app that was compiled with Spring Boot 2.0 to run with earlier versions. The agent adds additional `run` methods to `SpringApplication` and a constructor to `SpringApplicationBuilder` all of which match the signature of the same methods in Spring Boot 2.0 and delegate to the older methods that are actually on the classpath.

The sample is itself a demo of the agent. Example (just replace the main jar with your own):

```
$ mvn package
$ java -javaagent:target/spring-boot-legacy-agent-0.0.1-SNAPSHOT-agent.jar -jar target/spring-boot-legacy-agent-0.0.1-SNAPSHOT.jar --thin.profile=old
```

(runs with Spring Boot 1.5.x), and:

```
$ mvn package
$ java -jar target/spring-boot-legacy-agent-0.0.1-SNAPSHOT.jar --thin.profile=snapshot
```

runs with 2.0.x snapshots.

There are, of course, other binary API differences between Spring Boot 1.5 and 2.0, but if you only used the entry points in `SpringApplication` and `SpringApplicationBuilder` the agent will make your app portable.
