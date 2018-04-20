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

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		String instrumentedClassName = "org.springframework.boot.SpringApplication";
		String instrumentedMethodName = "run";
		byte[] bytecode = classfileBuffer;
		if (Boolean.getBoolean("agent.debug")
				&& className.startsWith("org/springframework")) {
			System.out.println("Inspecting " + className);
		}
		try {
			ClassPool classPool = ClassPool.getDefault();
			CtClass classDef = classPool.makeClass(new ByteArrayInputStream(bytecode));
			if (classDef.getName().equals(instrumentedClassName)) {
				Agent.addPathList(classPool, loader);
				try {
					classDef.getDeclaredMethod(instrumentedMethodName,
							new CtClass[] { classPool.getCtClass("java.lang.Class"),
									classPool.getCtClass("java.lang.String[]") });
				}
				catch (NotFoundException e) {
					System.out.println("Instrumenting " + classDef.getName());
					addMethod(classDef,
							"static ConfigurableApplicationContext run(Class source, String[] args) { return run((Object)source, args); }");
					addMethod(classDef,
							"static ConfigurableApplicationContext run(Class[] source, String[] args) { return run((Object[])source, args); }");
					bytecode = classDef.toBytecode();
				}
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return bytecode;
	}

	private void addMethod(CtClass classDef, String method) throws Exception {
		CtMethod classMethod = CtMethod.make(method, classDef);
		classMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.VARARGS);
		classDef.addMethod(classMethod);
	}

}
