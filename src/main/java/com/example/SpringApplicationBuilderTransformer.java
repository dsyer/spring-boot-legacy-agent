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
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationBuilderTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		String instrumentedClassName = "org.springframework.boot.builder.SpringApplicationBuilder";
		byte[] bytecode = classfileBuffer;
		try {
			ClassPool classPool = ClassPool.getDefault();
			CtClass classDef = classPool.makeClass(new ByteArrayInputStream(bytecode));
			if (classDef.getName().equals(instrumentedClassName)) {
				Agent.addPathList(classPool, loader);
				try {
					classDef.getDeclaredConstructor(
							new CtClass[] { classPool.getCtClass("java.lang.Class[]") });
				}
				catch (NotFoundException e) {
					System.out.println("Instrumenting " + classDef.getName());
					addConstructor(new CtClass[] { classPool.get("java.lang.Class[]") },
							classDef, "{this((Object[])$1);}");
					bytecode = classDef.toBytecode();
				}
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return bytecode;
	}

	private void addConstructor(CtClass[] params, CtClass classDef, String body)
			throws Exception {
		CtConstructor ctor = CtNewConstructor.make(params, new CtClass[0], body,
				classDef);
		ctor.setModifiers(Modifier.PUBLIC | Modifier.VARARGS);
		classDef.addConstructor(ctor);
	}

}
