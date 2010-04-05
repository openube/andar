#include <stdio.h>
#include <stdlib.h>
#include <AR/config.h>
#include <AR/param.h>
#include <AR/ar.h>
#include <AR/arGl.h>

#define   MINIWIN_MAX    8
#define   REVERSE_LR     1
#define   LEFTEYE        1
#define   RIGHTEYE       2
#define   GMINI          2

static ARParam  gCparam;
static int      gl_hmd_flag      = 0;
static double   gZoom;
static int      gXsize, gYsize;
static int      gMiniXnum,  gMiniYnum;
static int      gMiniXsize, gMiniYsize;
static int      gWinXsize, gWinYsize;
static int      gImXsize, gImYsize;
float   gl_cpara[16];


void argInit( ARParam *cparam, double zoom, int fullFlag, int xwin, int ywin, int hmd_flag )
{
    int       i;

    gl_hmd_flag = hmd_flag;
    gZoom  = zoom;
    gImXsize = cparam->xsize;
    gImYsize = cparam->ysize;
    if( gl_hmd_flag == 0 ) {
        gXsize = (double)cparam->xsize * gZoom;
        gYsize = (double)cparam->ysize * gZoom;
    }
    else {
        gXsize = AR_HMD_XSIZE;
        gYsize = AR_HMD_YSIZE;
    }
    gMiniXsize = (double)cparam->xsize * gZoom / GMINI;
    gMiniYsize = (double)cparam->ysize * gZoom / GMINI;

    if( xwin * ywin > MINIWIN_MAX ) {
        if( xwin > MINIWIN_MAX ) xwin = MINIWIN_MAX;
        ywin = MINIWIN_MAX / xwin;
    }
    gMiniXnum = xwin;
    gMiniYnum = ywin;
    gWinXsize = (gMiniXsize*gMiniXnum > gXsize)?
                     gMiniXsize*gMiniXnum: gXsize;
    gWinYsize = gYsize + gMiniYsize*gMiniYnum;

    gCparam = *cparam;
    for( i = 0; i < 4; i++ ) {
        gCparam.mat[1][i] = (gCparam.ysize-1)*(gCparam.mat[2][i]) - gCparam.mat[1][i];
    }
    argConvGLcpara( &gCparam, AR_GL_CLIP_NEAR, AR_GL_CLIP_FAR, gl_cpara );
}

void argConvGLcpara( ARParam *param, double gnear, double gfar, float m[16] )
{
    argConvGLcpara2( param->mat, param->xsize, param->ysize, gnear, gfar, m );
}

static void argConvGLcpara2( double cparam[3][4], int width, int height, double gnear, double gfar, float m[16] )
{
    double   icpara[3][4];
    double   trans[3][4];
    double   p[3][3], q[4][4];
    int      i, j;

    if( arParamDecompMat(cparam, icpara, trans) < 0 ) {
        printf("gConvGLcpara: Parameter error!!\n");
        exit(0);
    }

    for( i = 0; i < 3; i++ ) {
        for( j = 0; j < 3; j++ ) {
            p[i][j] = icpara[i][j] / icpara[2][2];
        }
    }
    q[0][0] = (2.0 * p[0][0] / width);
    q[0][1] = (2.0 * p[0][1] / width);
    q[0][2] = ((2.0 * p[0][2] / width)  - 1.0);
    q[0][3] = 0.0;

    q[1][0] = 0.0;
    q[1][1] = (2.0 * p[1][1] / height);
    q[1][2] = ((2.0 * p[1][2] / height) - 1.0);
    q[1][3] = 0.0;

    q[2][0] = 0.0;
    q[2][1] = 0.0;
    q[2][2] = (gfar + gnear)/(gfar - gnear);
    q[2][3] = -2.0 * gfar * gnear / (gfar - gnear);

    q[3][0] = 0.0;
    q[3][1] = 0.0;
    q[3][2] = 1.0;
    q[3][3] = 0.0;

    for( i = 0; i < 4; i++ ) {
        for( j = 0; j < 3; j++ ) {
            m[i+j*4] = q[i][0] * trans[0][j]
                     + q[i][1] * trans[1][j]
                     + q[i][2] * trans[2][j];
        }
        m[i+3*4] = q[i][0] * trans[0][3]
                 + q[i][1] * trans[1][3]
                 + q[i][2] * trans[2][3]
                 + q[i][3];
    }
}


void argConvGlpara( double para[3][4], float gl_para[16] )
{
    int     i, j;

    for( j = 0; j < 3; j++ ) {
        for( i = 0; i < 4; i++ ) {
            gl_para[i*4+j] = para[j][i];
        }
    }
    gl_para[0*4+3] = gl_para[1*4+3] = gl_para[2*4+3] = 0.0;
    gl_para[3*4+3] = 1.0;
}
