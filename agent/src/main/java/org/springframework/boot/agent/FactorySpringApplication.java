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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import net.bytebuddy.asm.Advice;

/**
 * @author Dave Syer
 *
 */
public class FactorySpringApplication {

	public static Map<Class<?>, Set<String>> mappings = new HashMap<>();

	public static Set<Class<?>> mapped = new HashSet<>();

	public static Collection<String> loadFactoryNames(Class<?> key) {
		if (mappings.containsKey(key)) {
			return mappings.get(key);
		}
		return PropertiesFileFactories.loadFactoryNames(key, null);
	}

	public static void clear() {
		mapped.clear();
		mappings.clear();
	}

	@Advice.OnMethodEnter
	public static void run(@Advice.This Object target,
			@Advice.Argument(0) String[] args) {
		SpringApplication app = (SpringApplication) target;
		Class<?>[] primarySources = stash(getField(target, "primarySources"));
		if (!FactorySpringApplication.mappings.isEmpty()) {
			for (SpringApplicationCustomizer customizer : factories(primarySources,
					SpringApplicationCustomizer.class)) {
				customizer.customize(app, args);
			}
		}
	}

	public static Class<?>[] getField(Object target, String name) {
		Field field = ReflectionUtils.findField(target.getClass(), name);
		ReflectionUtils.makeAccessible(field);
		return ((Set<Class<?>>) ReflectionUtils.getField(field, target))
				.toArray(new Class<?>[0]);
	}

	public static <T> Collection<? extends T> factories(Class<?>[] sources,
			Class<T> type) {
		List<T> result = new ArrayList<>();
		for (Class<?> source : sources) {
			if (FactorySpringApplication.mappings.containsKey(source)) {
				Set<String> map = FactorySpringApplication.mappings.get(source);
				result.addAll(createSpringFactoriesInstances(type, null, map));
			}
		}
		OrderComparator.sort(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<? extends T> createSpringFactoriesInstances(
			Class<T> type, ClassLoader classLoader, Set<String> names) {
		List<T> instances = new ArrayList<>(names.size());
		for (String name : names) {
			try {
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				Assert.isAssignable(type, instanceClass);
				Constructor<?> constructor = instanceClass
						.getDeclaredConstructor(new Class<?>[0]);
				T instance = (T) BeanUtils.instantiateClass(constructor);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}

	public static Class<?>[] stash(Class<?>[] primarySources) {
		// mappings.put(primarySources[0], Collections.emptySet());
		// return primarySources;
		for (Class<?> key : primarySources) {
			if (AnnotationUtils.isAnnotationDeclaredLocally(
					SpringApplicationCustomizers.class, key)) {
				computeFactories(key, AnnotationUtils.getAnnotation(key,
						SpringApplicationCustomizers.class));
			}
		}
		return primarySources;
	}

	public static void computeFactories(Class<?> type,
			SpringApplicationCustomizers factories) {
		if (!mapped.contains(type)) {
			resolveFactories(type, factories);
			for (Class<?> spec : factories.classes()) {
				if (AnnotationUtils.isAnnotationDeclaredLocally(
						SpringApplicationCustomizers.class, spec)) {
					computeFactories(type, AnnotationUtils.getAnnotation(spec,
							SpringApplicationCustomizers.class));
				}
			}
			mapped.add(type);
		}
	}

	public static void resolveFactories(Class<?> type,
			SpringApplicationCustomizers factories) {
		for (Class<?> attrs : findSelected(factories)) {
			FactorySpringApplication.mappings
					.computeIfAbsent(type, k -> new LinkedHashSet<>())
					.add(attrs.getName());
		}
	}

	public static Class<?>[] findSelected(SpringApplicationCustomizers factories) {
		return factories.classes();
	}

	public static class PropertiesFileFactories {

		public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

		private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();

		public static List<String> loadFactoryNames(Class<?> factoryClass,
				@Nullable ClassLoader classLoader) {
			String factoryClassName = factoryClass.getName();
			return loadSpringFactories(classLoader).getOrDefault(factoryClassName,
					Collections.emptyList());
		}

		private static Map<String, List<String>> loadSpringFactories(
				@Nullable ClassLoader classLoader) {
			MultiValueMap<String, String> result = cache.get(classLoader);
			if (result != null) {
				return result;
			}

			try {
				Enumeration<URL> urls = (classLoader != null
						? classLoader.getResources(FACTORIES_RESOURCE_LOCATION)
						: ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
				result = new LinkedMultiValueMap<>();
				while (urls.hasMoreElements()) {
					URL url = urls.nextElement();
					UrlResource resource = new UrlResource(url);
					Properties properties = PropertiesLoaderUtils
							.loadProperties(resource);
					for (Map.Entry<?, ?> entry : properties.entrySet()) {
						String factoryClassName = ((String) entry.getKey()).trim();
						for (String factoryName : StringUtils
								.commaDelimitedListToStringArray(
										(String) entry.getValue())) {
							result.add(factoryClassName, factoryName.trim());
						}
					}
				}
				cache.put(classLoader, result);
				return result;
			}
			catch (IOException ex) {
				throw new IllegalArgumentException(
						"Unable to load factories from location ["
								+ FACTORIES_RESOURCE_LOCATION + "]",
						ex);
			}
		}

	}

}
