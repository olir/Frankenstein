package de.serviceflow.frankenstein.plugin.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Proxy for native JNI calls with the capability to be loaded from external jar
 * with methods and callback to load a plug-in-specific native library with base
 * name PLUGINNAME. Convention is that the name of the package of the
 * implementing class ends with ".plugin.PLUGINNAME".
 */
public abstract class NativeJniProxy {

	/**
	 * Creates new instance and ensured the native library is loaded, by
	 * preparing it and invoking {@link #loadLibrary(String)}.
	 * 
	 * @throws UnsatisfiedLinkError if loading the native library failed.
	 */
	protected NativeJniProxy() throws UnsatisfiedLinkError {
		prepareLoadLibrary(this);
	}

	private void prepareLoadLibrary(NativeJniProxy invoker) throws UnsatisfiedLinkError {
		String packageName = invoker.getClass().getPackage().getName();
		String pluginName = packageName.substring(packageName.lastIndexOf(".plugin.") + 1);
		pluginName = pluginName.substring(0, pluginName.indexOf('.', pluginName.indexOf('.') + 1)).replace('.', '-');

		invoker.loadLibrary(prepareLoadLibrary(invoker.getClass(), pluginName));
	}

	private String prepareLoadLibrary(Class<?> invokerClass, String pluginName) throws UnsatisfiedLinkError {
		try {
			Class<?> c = invokerClass.getClassLoader().loadClass("de.serviceflow.frankenstein.LibraryManager");
			Object o = c.newInstance();
			Method m = c.getMethod("prepareLoadLibrary", Class.class, String.class);
			return (String) m.invoke(o, invokerClass, pluginName);
		} catch (SecurityException | ClassNotFoundException | NoSuchMethodException | InstantiationException
				| IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
			throw new UnsatisfiedLinkError(
					"Prepare to load library failed for " + pluginName + " with " + invokerClass.getName());
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t != null)
				t.printStackTrace();
			e.printStackTrace();
			throw new UnsatisfiedLinkError(
					"Prepare to load library failed for " + pluginName + " with " + invokerClass.getName());
		}
	}

	/**
	 * The Implementing class should call "System.loadLibrary(name);" here.
	 * Remark: The design of Java is to use reflection and internal class loader
	 * methods.
	 * 
	 * @param name
	 *            Name of the Library, including base name and architecture.
	 */
	protected abstract void loadLibrary(String name);
}
