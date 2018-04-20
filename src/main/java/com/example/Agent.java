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

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.NotFoundException;

/**
 * @author Dave Syer
 *
 */
public class Agent {

	private static Instrumentation inst;

	private static Set<ClassLoader> loaders = new HashSet<>();

	public static void premain(String args, Instrumentation inst) throws Exception {
		if (Agent.inst == null) {
			Agent.inst = inst;
			inst.addTransformer(new SpringApplicationBuilderTransformer());
			inst.addTransformer(new SpringApplicationTransformer());
		}
	}

	public static void agentmain(String args, Instrumentation inst) throws Exception {
		premain(args, inst);
	}

	public static void addPathList(ClassPool pool, ClassLoader loader)
			throws NotFoundException {
		if (loaders.contains(loader)) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		if (loader instanceof URLClassLoader) {
			for (URL url : ((URLClassLoader) loader).getURLs()) {
				if (builder.length() > 0) {
					builder.append(File.pathSeparatorChar);
				}
				builder.append(getFile(url.toString()));
			}
			pool.appendPathList(builder.toString());
			pool.importPackage("org.springframework.boot.builder");
			pool.importPackage("org.springframework.context");
		}
		loaders.add(loader);
	}

	private static String getFile(String url) {
		if (url.startsWith("file:")) {
			url = url.substring("file:".length());
			if (url.startsWith("//")) {
				url = url.substring(2);
			}
		}
		return url;
	}

}
