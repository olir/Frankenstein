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

JNIEXPORT void JNICALL Java_de_screenflow_frankenstein_vf_jni_MatBlender_init
  (JNIEnv* env, jobject obj)
{
  JwMat* mat = JwMat::matptr;
  if (mat == NULL) {
	 JwMat::matptr = new JwMat(env);
  }
}

JNIEXPORT void JNICALL Java_de_screenflow_frankenstein_vf_jni_MatBlender_process
  (JNIEnv* env, jobject obj,
   jobject matobj, jint frameId, jobject context, jobject overlaymatobj)
{
  JwMat* mat = JwMat::matptr;
  int cols = mat->cols(env, matobj);
  int rows = mat->rows(env, matobj);
//  cout << "rows=" << rows << ", cols=" << cols << endl;

  int channels = mat->channels(env, matobj);
  int channels2 = mat->channels(env, overlaymatobj);
  if (channels<3) {
	  J_THROW("java/lang/Error", "Expecting BGR Type for underlay Mat. Channels < 3: "+mat->channels(env, matobj));
      return;
  }
  if (channels2<4) {
	  J_THROW("java/lang/Error", "Expecting ABGR Type for overlay Mat. Channels < 4: "+mat->channels(env, overlaymatobj));
      return;
  }


  for(int y = 0; y < rows; y++)
  {
	  jbyte * rowaddr = ROW_ADDR(env, matobj,mat,y);
	  jbyte * rowaddr2 = ROW_ADDR(env, overlaymatobj,mat,y);

	  for (int x = 0; x < cols; x++)
	  {
		  int i = x * channels;

		  int b = (unsigned char)rowaddr[i];
		  int g = (unsigned char)rowaddr[i+1];
		  int r = (unsigned char)rowaddr[i+2];

		  int a2 = (unsigned char)rowaddr2[i];
		  int b2 = (unsigned char)rowaddr2[i+1];
		  int g2 = (unsigned char)rowaddr2[i+2];
		  int r2 = (unsigned char)rowaddr2[i+3];

      rowaddr[i]   = (b * (255-a2) + b2 * a2 ) / 255;
      rowaddr[i+1] = (g * (255-a2) + g2 * a2 ) / 255;
      rowaddr[i+2] = (r * (255-a2) + r2 * a2 ) / 255;
	  }
  }

}

