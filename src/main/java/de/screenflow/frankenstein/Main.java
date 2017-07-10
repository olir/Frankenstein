package de.screenflow.frankenstein;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) {

		System.out.println("Args: "+Arrays.toString(args));

		Configuration c = Configuration.configure(args);

		// todo

		Class<?> fxMain;
		try {
			fxMain = Class.forName("de.screenflow.frankenstein.fxml.FxMain");
			Class<?> parameterTypes [] = {};
			Method main = fxMain.getDeclaredMethod("fxmain", parameterTypes);
			Object [] invokeArgs = {};
			main.invoke(fxMain, invokeArgs);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
