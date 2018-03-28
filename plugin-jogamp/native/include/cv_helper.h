#ifndef CV_HELPER_H
#define CV_HELPER_H

#define ROW_ADDR(ENV,OBJ,JWMAT,Y) ((jbyte *)(JWMAT->dataAddr(ENV, OBJ) + (JWMAT->step1(ENV, OBJ)) * (Y)))
#define POINT_CHANNEL_VALUE(ENV,OBJ,JWMAT,Y,X,CHANNEL) ROW_ADDR(ENV,OBJ,JWMAT,Y)[(X) * (JWMAT->channels(ENV,OBJ)) + CHANNEL]
#define CLAMP(value, low, high) (((value)<(low))?(low):(((value)>(high))?(high):(value)))
#endif

