package de.serviceflow.frankenstein.plugin.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class NativeFilter {

	protected NativeFilter() throws UnsatisfiedLinkError {
		prepareLoadLibrary(this);
	}

	private void prepareLoadLibrary(NativeFilter invoker) throws UnsatisfiedLinkError {
		String packageName = invoker.getClass().getPackage().getName();
		String pluginName = packageName.substring(packageName.lastIndexOf(".plugin.") + 1);
		pluginName = pluginName.substring(0, pluginName.indexOf('.', pluginName.indexOf('.') + 1)).replace('.', '-');

		invoker.loadLibrary(prepareLoadLibrary(invoker.getClass(), pluginName));
	}

	private String prepareLoadLibrary(Class<?> invokerClass, String name) throws UnsatisfiedLinkError {
		try {
			Class<?> c = invokerClass.getClassLoader().loadClass("de.serviceflow.frankenstein.LibraryManager");
			Object o = c.newInstance();
			Method m = c.getMethod("prepareLoadLibrary", Class.class, String.class);
			return (String) m.invoke(o, invokerClass, name);
		} catch (SecurityException | ClassNotFoundException | NoSuchMethodException | InstantiationException
				| IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
			throw new UnsatisfiedLinkError("Prepare to load library failed for " + name + " with " + invokerClass.getName());
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t!=null)
				t.printStackTrace();
			e.printStackTrace();
			throw new UnsatisfiedLinkError("Prepare to load library failed for " + name + " with " + invokerClass.getName());
		}
	}

	protected abstract void loadLibrary(String name);
}
