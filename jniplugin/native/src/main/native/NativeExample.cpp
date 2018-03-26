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

JNIEXPORT void JNICALL Java_cc0_NativeExample_init
  (JNIEnv* env, jobject obj)
{
  JwMat* mat = JwMat::matptr;
  cout << "Java_cc0_NativeExample_init START" << endl;
  if (mat == NULL) {
	 cout << "Java_cc0_NativeExample_init Mat-Wrapper created" << endl;
	 JwMat::matptr = new JwMat(env);
  }
  cout << "Java_cc0_NativeExample_init END" << endl;
}

JNIEXPORT void JNICALL Java_cc0_NativeExample_process
  (JNIEnv* env, jobject obj,
   jobject matobj, jint frameId, jobject context)
{
  cout << "Java_cc0_NativeExample_process CALLED " << frameId << endl;
  JwMat* mat = JwMat::matptr;
  int cols = mat->cols(env, matobj);
  int rows = mat->rows(env, matobj);
  cout << "rows=" << rows << ", cols=" << cols << endl;

  int channels = mat->channels(env, matobj);
  if (channels<3) {
	  J_THROW("java/lang/Error", "channels < 3: "+mat->channels(env, matobj));
      return;
  }

  int c0 = (unsigned char)POINT_CHANNEL_VALUE(env,matobj,mat,(rows>>1),(cols>>1),0);
  int c1 = (unsigned char)POINT_CHANNEL_VALUE(env,matobj,mat,(rows>>1),(cols>>1),1);
  int c2 = (unsigned char)POINT_CHANNEL_VALUE(env,matobj,mat,(rows>>1),(cols>>1),2);
  cout << "RGB-values at " << (rows>>1) << "," << (cols>>1) << ": " <<
		  c2 << "/" << c1 << "/" << c0 << endl ;

}

