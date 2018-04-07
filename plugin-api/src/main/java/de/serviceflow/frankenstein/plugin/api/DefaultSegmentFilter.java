package de.serviceflow.frankenstein.plugin.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Basic implementation of a SegmentVideoFilter.
 */
public abstract class DefaultSegmentFilter implements SegmentVideoFilter {
	private DefaultSegmentConfigController configController = null;

	private final String identifier;
	private final PropertyResourceBundle bundleConfiguration;
	private final String configManagerClass;

	/**
	 * Created an instance for the given IDENTIFIER. Convention is that in the
	 * package of the implementing class an fxml file of name IDENTIFIER.fxml
	 * and a property file of name IDENTIFIER.properties (and optionally
	 * localizations) exists.
	 * 
	 * The ConfigManager implementation
	 * de.serviceflow.frankenstein.Configuration is used.
	 * 
	 * @param identifier
	 *            simple identifier name for this filter.
	 */
	protected DefaultSegmentFilter(String identifier) {
		this(identifier, "de.serviceflow.frankenstein.Configuration");
	}

	/**
	 * Like {@link DefaultSegmentFilter#DefaultSegmentFilter(String)}, but with
	 * the option to specify an individual configManagerClass.
	 * 
	 * @param identifier simple identifier name for this filter.
	 * @param configManagerClass
	 *            full qualified class name to a ConfigManager implementation.
	 */
	protected DefaultSegmentFilter(String identifier, String configManagerClass) {
		this.identifier = identifier;
		this.configManagerClass = configManagerClass;

		bundleConfiguration = (PropertyResourceBundle) ResourceBundle.getBundle(
				this.getClass().getPackage().getName().replace('.', '/') + '/' + identifier, getLocale(),
				this.getClass().getClassLoader());
	}

	private Locale getLocale() {
		return getConfigManager().getLocale();
	}

	protected ConfigManager getConfigManager() {
		Class<?> fxMain;
		try {
			fxMain = Class.forName(configManagerClass);
			Class<?> parameterTypes[] = {};
			Method main = fxMain.getDeclaredMethod("getInstance", parameterTypes);
			Object[] invokeArgs = {};
			return (ConfigManager) main.invoke(fxMain, invokeArgs);
		} catch (Throwable e) {
			throw new Error(e);
		}
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(identifier + ".fxml"), bundleConfiguration);
		configController = instantiateController();
		loader.setController(configController);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		initializeController(); // custom initialization possible here
		return scene;
	}

	/**
	 * Returns, if necessary creates, the controller instance.
	 * 
	 * @return SegmentConfigController
	 */
	abstract protected DefaultSegmentConfigController instantiateController();

	/**
	 * initializing the current controller.
	 */
	abstract protected void initializeController();

	public DefaultSegmentConfigController getConfigController() {
		return configController;
	}
}
