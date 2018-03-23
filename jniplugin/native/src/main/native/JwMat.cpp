#include <iostream>            // for cout and cin

#include <jni.h>
#include "JwMat.h"

using namespace std;

JwMat::JwMat(JNIEnv* env)
{
	cout << "JwMat() START" << endl;
    clsMat = env->FindClass("org/opencv/core/Mat");
    m_rows = env->GetMethodID(clsMat, "rows",
                              "()I");
    m_cols = env->GetMethodID(clsMat, "cols",
                              "()I");
    m_channels = env->GetMethodID(clsMat, "channels",
                                  "()I");
    m_dataAddr = env->GetMethodID(clsMat, "dataAddr",
                                  "()J");
    m_step1 = env->GetMethodID(clsMat, "step1",
                               "()J");
	cout << "JwMat() END" << endl;
}

JwMat::~JwMat()
{
}

int JwMat::rows(JNIEnv* env, jobject matobj) {
	return env->CallIntMethod(matobj, m_rows);
}

int JwMat::cols(JNIEnv* env, jobject matobj) {
	return env->CallIntMethod(matobj, m_cols);
}

int JwMat::channels(JNIEnv* env, jobject matobj) {
	return env->CallIntMethod(matobj, m_channels);
}

long JwMat::dataAddr(JNIEnv* env, jobject matobj) {
	return env->CallLongMethod(matobj, m_dataAddr);
}

long JwMat::step1(JNIEnv* env, jobject matobj) {
	return env->CallLongMethod(matobj, m_step1);
}
