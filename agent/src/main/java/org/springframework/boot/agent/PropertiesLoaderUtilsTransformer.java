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

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

/**
 * @author Dave Syer
 *
 */
public class PropertiesLoaderUtilsTransformer implements Transformer {

	@Override
	public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
			ClassLoader classLoader, JavaModule module) {
		try {
			builder = builder
					.method(named("loadProperties").and(isStatic()).and(isPublic()))
					.intercept(Advice.to(Interceptor.class));
			return builder;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static class Interceptor {

		public static final String[] EXCLUDES = { ApplicationListener.class.getName(),
				ApplicationContextInitializer.class.getName() };

		@Advice.OnMethodExit
		public static void loadProperties(@Advice.Return Properties props)
				throws IOException {
			Properties result = new Properties();
			result.putAll(props);
			for (String name : EXCLUDES) {
				if (result.containsKey(name)) {
					props.remove(name);
				}
			}
		}

	}

}
