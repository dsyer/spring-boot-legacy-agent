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
package com.example;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * @author Dave Syer
 *
 */
public class Agent {

	public static void premain(String args, Instrumentation inst) throws Exception {
		new AgentBuilder.Default().with(new Listener.Adapter() {
			@Override
			public void onError(String typeName, ClassLoader classLoader,
					JavaModule module, boolean loaded, Throwable throwable) {
				throwable.printStackTrace();
			}
		}).type(ElementMatchers.named("org.springframework.boot.SpringApplication"))
				.transform(new SpringApplicationTransformer()) //
				.type(ElementMatchers.named(
						"org.springframework.boot.builder.SpringApplicationBuilder"))
				.transform(new SpringApplicationBuilderTransformer()) //
				.installOn(inst);
	}

	public static void agentmain(String args, Instrumentation inst) throws Exception {
		premain(args, inst);
	}

}
