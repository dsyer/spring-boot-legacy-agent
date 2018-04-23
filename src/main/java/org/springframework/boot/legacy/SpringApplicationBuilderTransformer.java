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
package org.springframework.boot.legacy;

import java.lang.reflect.Modifier;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.utility.JavaModule;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationBuilderTransformer implements Transformer {

	@Override
	public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
			ClassLoader classLoader, JavaModule module) {
		try {
			MethodList<InDefinedShape> methods = typeDescription.getDeclaredMethods()
					.filter(isConstructor().and(takesArguments(Object[].class)));
			if (!methods.isEmpty()) {
				return builder.defineConstructor(Modifier.PUBLIC)
						.withParameter(Class[].class)
						.intercept(MethodCall.invoke(methods.get(0)).withAllArguments());
			}
			return builder;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
