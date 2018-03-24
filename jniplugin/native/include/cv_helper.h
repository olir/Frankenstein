#ifndef CV_HELPER_H
#define CV_HELPER_H

#define ROW_ADDR(Y,JWMAT) ((jbyte *)(JWMAT->dataAddr(env, matobj) + JWMAT->step1(env, matobj) * Y))
#define POINT_CHANNEL_VALUE(X,Y,CHANNEL,JWMAT) ROW_ADDR(Y,JWMAT)[X * JWMAT->channels(env, matobj) + CHANNEL]
#define CLAMP(value, low, high) (((value)<(low))?(low):(((value)>(high))?(high):(value)))
#endif

