/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.agent;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationAgent {

	public static void install() throws Exception {
		premain(null, ByteBuddyAgent.install());
	}

	public static void premain(String args, Instrumentation inst) throws Exception {
		new AgentBuilder.Default()
				.with(AgentBuilder.Listener.StreamWriting.toSystemError()
						.withTransformationsOnly())
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(named("org.springframework.boot.SpringApplication"))
				.transform(new SpringApplicationTransformer()) //
				.type(named("org.springframework.core.io.support.PropertiesLoaderUtils"))
				.transform(new PropertiesLoaderUtilsTransformer()) //
				.installOn(inst);
	}

	public static void agentmain(String args, Instrumentation inst) throws Exception {
		premain(args, inst);
	}

}
