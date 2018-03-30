/*
 * Copyright 2017 Oliver Rode, https://github.com/olir/Frankenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.frankenstein.plugin.api;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public abstract class NativeSegmentFilter<C> extends DefaultSegmentFilter implements SegmentVideoFilter {
	// private static URLClassLoader loader = null;

	@SuppressWarnings("rawtypes")
	private final Class jniProxyClass;
	private final Object jniProxy;
	private final Method jniProxyInitMethod;

	@SuppressWarnings("unchecked")
	protected NativeSegmentFilter(String identifier, String proxyClassName) {
		super(identifier);

		try {
			// use dynamic loading and reflection when loading jni proxy class
			// from jar, so app do not depend on it.
			// URLClassLoader childLoader = getLoader();
			jniProxyClass = Class.forName(proxyClassName, true, this.getClass().getClassLoader());
			jniProxy = jniProxyClass.newInstance();
			jniProxyInitMethod = jniProxyClass.getMethod("init");
			jniProxyInitMethod.invoke(jniProxy);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
				| SecurityException | IllegalArgumentException | InvocationTargetException /* | MalformedURLException */ e) {
			throw new RuntimeException(
					"jni wrapper creation failed. Bug-Mining: Ensure the wrapper was added to the javahClassNames in pom.xml. Check NativeCode.h for existing and proper signatures.",
					e);
		}
	}
/*
	static synchronized URLClassLoader getLoader() throws MalformedURLException {
		if (loader == null) {
			final String RELEATIVE_TO_MAVEN_EXEC_CWD = "../../../../jniplugin/target";
			String pluginpath = System.getProperty("pluginpath", RELEATIVE_TO_MAVEN_EXEC_CWD);
			File myJar = new File(new File(pluginpath), "jniplugin-java-0.1.1-SNAPSHOT.jar");
			URL[] urls = new URL[] { myJar.toURI().toURL() };
			loader = new URLClassLoader(urls, NativeSegmentFilter.class.getClassLoader());
		}
		return loader;
	}
*/
	@SuppressWarnings("rawtypes")
	protected Class getJniProxyClass() {
		return jniProxyClass;
	}

	protected Object getJniProxy() {
		return jniProxy;
	}
}
