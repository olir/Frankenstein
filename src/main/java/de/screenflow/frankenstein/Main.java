package de.screenflow.frankenstein;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) {

		System.out.println("Args: " + Arrays.toString(args));

		Configuration c = Configuration.cliCreateConfiguration(args);
		if (c == null) {
			System.out.println(Configuration.getUsage());
			System.exit(0);
		}

		if (args.length==0 || c.isVisual()) {

			Class<?> fxMain;
			try {
				fxMain = Class.forName("de.screenflow.frankenstein.fxml.FxMain");
				Class<?> parameterTypes[] = {de.screenflow.frankenstein.Configuration.class};
				Method main = fxMain.getDeclaredMethod("fxmain", parameterTypes);
				Object[] invokeArgs = {c};
				main.invoke(fxMain, invokeArgs);
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}

		}
	}
}
