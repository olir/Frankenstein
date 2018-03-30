package de.serviceflow.frankenstein.plugin.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class NativeFilter {
  private static boolean loaderCalled = false;
  private static UnsatisfiedLinkError error = null;
  private static final Set<String> loadedLibraries = Collections.synchronizedSet(new HashSet<String>());

  public static void loadLibrary(Class invokerClass) throws UnsatisfiedLinkError {
    if (!loaderCalled) {
      loaderCalled = true;
      System.out.println("Working Directory = " + new File(".").getAbsolutePath());
      try {
        if (System.getProperty("os.arch").contains("64")
            && System.getProperty("sun.arch.data.model").contains("64")) {
          // load 64-bit lib
          loadLibrary(invokerClass, "jniplugin-64");
        } else {
          // load 32-bit lib
          loadLibrary(invokerClass, "jniplugin-32");
        }
      } catch (UnsatisfiedLinkError t) {
        System.out.println("sun.arch.data.model=" + System.getProperty("sun.arch.data.model"));
        System.out.println("os.arch=" + System.getProperty("os.arch"));

        error = t;
        throw t;
      }
      if (error != null)
        throw error; // throw again
    }
  }

  private static void loadLibrary(Class invokerClass, String libraryName) throws UnsatisfiedLinkError {
    synchronized (loadedLibraries) {

      if (loadedLibraries.contains(libraryName.intern()))
        return;

      try {
        System.loadLibrary(libraryName);
      } catch (final UnsatisfiedLinkError e) {
        if (!String.format("no %s in java.library.path", libraryName).equals(e.getMessage())) {
          System.out.println(
                  "!!! [1] UnsatisfiedLinkError @ libraryName='" + libraryName + " message: "+e.getMessage()+", but lookup seems to successful. java.library.path="+System.getProperty("java.library.path"));
          throw e;
        }

        String libFileName = libraryName + ".dll";
        String location = "/" + libFileName;
        InputStream binary = invokerClass.getResourceAsStream("/" + libFileName);
        if (binary == null)
          throw new Error("binary not found: " + "/" + libFileName);


        try {
          Path tmpDir = Files.createTempDirectory("frankenstein");
          tmpDir.toFile().deleteOnExit();
          Path destination = tmpDir.resolve("./" + location).normalize();

          try {
            Files.createDirectories(destination.getParent());
            Files.copy(binary, destination);
            String nPath = destination.getParent().normalize().toString();

            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);

            Set<String> myPath = new HashSet<String>(Arrays.asList((String[]) field.get(null)));
            myPath.add(nPath);

            field.set(null, myPath.toArray(new String[myPath.size()]));

            System.setProperty("java.library.path",
            System.getProperty("java.library.path") + File.pathSeparator + nPath);
          } catch (IllegalAccessException x) {
            throw new Error("IllegalAccessException!?", x);
          } catch (NoSuchFieldException x) {
            throw new Error("NoSuchFieldException!?", x);
          }

          try {
        	//System.load(destination.normalize().toString());
            System.loadLibrary(libraryName);
          } catch (UnsatisfiedLinkError x) {
              System.out.println("!!! [1] UnsatisfiedLinkError: "+e.getMessage()+". But found binary in classpath -> Relocated to "+destination);
              System.out.println(
                  "!!! [2] UnsatisfiedLinkError @ libraryName='" + libraryName + " message: "+x.getMessage());
             throw x;
          }

          loadedLibraries.add(libraryName.intern());

        } catch (final IOException x) {
          throw new Error("Error writing native library", x);
        }
      }
    }
  }

  protected NativeFilter() throws UnsatisfiedLinkError {
    loadLibrary(this.getClass());
  }

}
