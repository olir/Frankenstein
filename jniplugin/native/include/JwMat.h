#ifndef JWMAT_H
#define JWMAT_H

#include <jni.h>

class JwMat                    // begin declaration of the class
{
  public:                      // begin public section
    JwMat(JNIEnv* env);     	// constructor
    ~JwMat();                  // destructor
    int rows(JNIEnv* env, jobject matobj);
    int cols(JNIEnv* env, jobject matobj);
    int channels(JNIEnv* env, jobject matobj);
    long dataAddr(JNIEnv* env, jobject matobj);
    long step1(JNIEnv* env, jobject matobj);
 private:                      // begin private section
    jclass clsMat;
    jmethodID m_rows;
    jmethodID m_cols;
    jmethodID m_channels;
    jmethodID m_dataAddr;
    jmethodID m_step1;
};

#endif

