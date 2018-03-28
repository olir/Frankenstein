#ifndef JNI_HELPER_H
#define JNI_HELPER_H

#define J_THROW(classname,text) env->ThrowNew(env->FindClass(classname), text);


#endif

