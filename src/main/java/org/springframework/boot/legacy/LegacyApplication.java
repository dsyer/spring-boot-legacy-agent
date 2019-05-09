/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.legacy;

import java.io.File;
import java.lang.management.ManagementFactory;

import com.sun.tools.attach.VirtualMachine;

public class LegacyApplication {

	private static String agentFile = new File(System.getProperty("user.home")
			+ "/.m2/repository/org/springframework/boot/experimental/spring-boot-legacy-agent/"
			+ "0.0.1-SNAPSHOT/spring-boot-legacy-agent-0.0.1-SNAPSHOT-agent.jar")
					.getAbsolutePath();

	public static void attach() {
		String pid = pid();
		if (pid != null) {
			try {
				VirtualMachine jvm = VirtualMachine.attach(pid);
				jvm.loadAgent(agentFile);
				jvm.detach();
			}
			catch (Exception e) {
				throw new IllegalStateException("Cannot attach to " + pid, e);
			}

		}
	}

	private static String pid() {
		try {
			String jvmName = ManagementFactory.getRuntimeMXBean().getName();
			return jvmName.split("@")[0];
		}
		catch (Throwable ex) {
			return null;
		}
	}

}
