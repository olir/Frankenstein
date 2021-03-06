#include <stdio.h>
#include <stdlib.h>
#include <iostream>

#include "nativeCode.h" // generated by javah via maven-native-plugin

#include "JwMat.h"
#include "jni_helper.h"
#include "cv_helper.h"

using namespace std;

JNIEXPORT void JNICALL Java_de_serviceflow_frankenstein_plugin_opencv_jni_VideoEqualizer_init
  (JNIEnv* env, jobject obj)
{
  JwMat* mat = JwMat::matptr;
  if (mat == NULL) {
	 JwMat::matptr = new JwMat(env);
  }
}

JNIEXPORT void JNICALL Java_de_serviceflow_frankenstein_plugin_opencv_jni_VideoEqualizer_process
  (JNIEnv* env, jobject obj,
   jobject matobj, jint frameId, jobject context, jint brightness, jint contrast, jint saturation)
{
  JwMat* mat = JwMat::matptr;
  int cols = mat->cols(env, matobj);
  int rows = mat->rows(env, matobj);
//  cout << "rows=" << rows << ", cols=" << cols << endl;

  int channels = mat->channels(env, matobj);
  if (channels<3) {
	  J_THROW("java/lang/Error", "Expecting HSV Mat. channels < 3: "+mat->channels(env, matobj));
      return;
  }


  for(int y = 0; y < rows; y++)
  {
	  jbyte * rowaddr = ROW_ADDR(env, matobj,mat,y);

	  for (int x = 0; x < cols; x++)
	  {
		  int i = x * channels;

		  // OpenCV: For HSV, Hue range is [0,179], Saturation range is [0,255] and Value range is [0,255].
		  int h = (unsigned char)rowaddr[i];
		  int s = (unsigned char)rowaddr[i+1];
		  int v = (unsigned char)rowaddr[i+2];

		  // Apply brightness
		  int br = 255 * (brightness - 50) / 50; // -255 .. 255
		  v = v + br;
	      v = CLAMP(v, 0, 255);

		  // Apply saturation
		  int sa = 255 * (saturation - 50) / 50; // -255 .. 255
		  s = s + sa;
	      s = CLAMP(s, 0, 255);

		  // Apply contrast
		  int cdynamic = 4;
		  int co = 255 * (contrast - 50) / 50; // -255 .. 255
		  int vm = v-128;   // -128 .. 127
		  if (co>0) {
			  if (vm==0)
				  vm = 1;  // BW-split
			  vm = vm * (255+co*cdynamic) / 255;
		  }
		  else if (co<0)
		  {
			  vm = vm * 255 / (255-co*cdynamic);
		  }
		  v = vm + 128;
	      v = CLAMP(v, 0, 255);

	      //	unchanged:      rowaddr[i] = h;
		  rowaddr[i+1] = s; // save result
		  rowaddr[i+2] = v; // save result
	  }
  }

}

