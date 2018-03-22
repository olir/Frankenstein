#ifndef JWMAT_H
#define JWMAT_H

#include <jni.h>

class JwMat                    // begin declaration of the class
{
  public:                      // begin public section
    JwMat(JNIEnv* env);     	// constructor
    ~JwMat();                  // destructor
 private:                      // begin private section
    JNIEnv * env;                // member variable
};


#endif