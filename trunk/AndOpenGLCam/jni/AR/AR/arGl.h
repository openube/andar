#include <AR/config.h>
#include <AR/param.h>
#include <AR/ar.h>

//functions:
static void argConvGLcpara2( double cparam[3][4], int width, int height, double gnear, double gfar, float m[16] );
void argInit( ARParam *cparam, double zoom, int fullFlag, int xwin, int ywin, int hmd_flag );
void argConvGLcpara( ARParam *param, double gnear, double gfar, float m[16] );
void argConvGlpara( double para[3][4], float gl_para[16] );
