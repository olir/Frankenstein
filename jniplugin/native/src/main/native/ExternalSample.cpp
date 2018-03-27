#include <stdio.h>
#include <stdlib.h>
#include <iostream>

#include "nativeCode.h" // generated by javah via maven-native-plugin

#include "opencv2/core/core.hpp"
#include "JwMat.h"
#include "jni_helper.h"
#include "cv_helper.h"

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_de_screenflow_frankenstein_vf_external_ExternalSample_init
  (JNIEnv* env, jobject obj)
{
  JwMat* mat = JwMat::matptr;
  if (mat == NULL) {
	 JwMat::matptr = new JwMat(env);
  }
}

JNIEXPORT void JNICALL Java_de_screenflow_frankenstein_vf_external_ExternalSample_process
  (JNIEnv* env, jobject obj,
   jobject matobj, jint frameId, jobject context)
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

        // ...
	  }
  }

}

