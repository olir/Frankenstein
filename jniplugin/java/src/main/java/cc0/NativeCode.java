package cc0;

public class NativeCode
{
public native void helloNative();

public static void main(String[] args)
{
    try { 
        if(System.getProperty("os.arch").contains("64") && System.getProperty("sun.arch.data.model").contains("64")) {
             // load 64-bit lib
             System.loadLibrary("jniplugin-64");
        }
        else {
           // load 32-bit lib
           System.loadLibrary("jniplugin-32");
        }
    }
    catch (Throwable t) {
      System.out.println("sun.arch.data.model="+System.getProperty("sun.arch.data.model"));
      System.out.println("os.arch="+System.getProperty("os.arch"));
     
      t.printStackTrace();
    }
  
    new NativeCode().helloNative();
    }
}
