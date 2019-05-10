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

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isVirtual;
import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.commons.ClassRemapper;
import net.bytebuddy.jar.asm.commons.SimpleRemapper;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationTransformer implements Transformer {

	@Override
	public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
			ClassLoader classLoader, JavaModule module) {
		try {
			return builder.visit(Advice.to(FactorySpringApplication.class)
					.on(isMethod().and(named("run").and(isVirtual()))));
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}

class SpringApplicationTypeRemapper extends AsmVisitorWrapper.AbstractBase {

	@Override
	public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
			Context implementationContext, TypePool typePool,
			FieldList<InDefinedShape> fields, MethodList<?> methods, int writerFlags,
			int readerFlags) {
		return new ClassRemapper(classVisitor,
				new SimpleRemapper("org/springframework/boot/SpringApplication",
						"org/springframework/boot/factory/FactorySpringApplication"));
	}

}
