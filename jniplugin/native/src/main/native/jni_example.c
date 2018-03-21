#include <stdio.h>
#include <stdlib.h>
#include "jni.h"
#include <nativeCode.h> // generated by javah via maven-native-plugin

JNIEXPORT void JNICALL Java_cc0_NativeCode_helloNative
  (JNIEnv * env, jobject obj)
{
    puts("Hello from C!");
}