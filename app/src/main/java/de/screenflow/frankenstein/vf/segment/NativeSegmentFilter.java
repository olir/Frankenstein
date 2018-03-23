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
package de.screenflow.frankenstein.vf.segment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.opencv.core.Mat;

import de.screenflow.frankenstein.fxml.FxMain;
import de.screenflow.frankenstein.vf.SegmentVideoFilter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public abstract class NativeSegmentFilter<C> implements SegmentVideoFilter {
	private static URLClassLoader loader = null;

	private C configController = null;

	private final String identifier;
	private final PropertyResourceBundle bundleConfiguration;
	private final Class jniProxyClass;
	private final Object jniProxy;
	private final Method jniProxyInitMethod;

	protected NativeSegmentFilter(String identifier, String proxyClassName) {
		this.identifier = identifier;

		bundleConfiguration = (PropertyResourceBundle) ResourceBundle
				.getBundle(getClass().getPackage().getName().replace('.', '/') + '/' + identifier, FxMain.getLocale());

		try {
			// use dynamic loading and reflection when loading jni proxy class
			// from jar, so app do not depend on it.
			URLClassLoader childLoader = getLoader();
			jniProxyClass = Class.forName(proxyClassName, true, childLoader);
			jniProxy = jniProxyClass.newInstance();
			jniProxyInitMethod = jniProxyClass.getMethod("init");
			jniProxyInitMethod.invoke(jniProxy);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
				| SecurityException | IllegalArgumentException | InvocationTargetException | MalformedURLException e) {
			throw new RuntimeException("jni wrapper creation failed. Bug-Mining: Ensure the wrapper was added to the javahClassNames in pom.xml. Check NativeCode.h for existing and proper signatures.", e);
		}
	}

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

	public final String toString() {
		return bundleConfiguration.getString("name");
	}

	@Override
	public final SegmentVideoFilter createInstance() {
		try {
			return getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final Scene createConfigurationScene(String stylesheet) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(identifier + ".fxml"), bundleConfiguration);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		configController = loader.getController();
		initializeController(); // custom initialization possible here
		return scene;
	}

	abstract protected void initializeController();

	protected final C getConfigController() {
		return configController;
	}

	@Override
	public final Mat configure(Mat firstFrame) {
		return firstFrame;
	}

	protected Class getJniProxyClass() {
		return jniProxyClass;
	}

	protected Object getJniProxy() {
		return jniProxy;
	}
}
