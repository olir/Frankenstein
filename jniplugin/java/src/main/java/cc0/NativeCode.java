package cc0;

public class NativeCode
{
public native void helloNative();

public static void main(String[] args)
{
if(System.getProperty("os.arch").contains("64")) {
   // load 64-bit lib
   System.loadLibrary("frankenstein-jniplugin-64");
}
else {
   // load 32-bit lib
   System.loadLibrary("frankenstein-jniplugin-32");
}
new NativeCode().helloNative();
}
}
