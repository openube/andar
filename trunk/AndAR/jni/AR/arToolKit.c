 /**
	Copyright (C) 2009  Tobias Domhan

    This file is part of AndOpenGLCam.

    AndObjViewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AndObjViewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AndObjViewer.  If not, see <http://www.gnu.org/licenses/>.
 
 */
/*
 * arToolKit.c
 * author: Tobias Domhan
 * license: GPL
 * This is the glue between the Java and the C part.
 */

#include <GLES/gl.h>
#include <stdio.h>
#include <AR/ar.h>
#include <AR/param.h>
#include <../marker_info.h>
#include <android/log.h>
#include <stdlib.h>

int             xsize, ysize;
int             thresh = 100;
int             count = 0;

char           *cparam_name    = "/sdcard/andar/camera_para.dat";
ARParam         cparam;

//pattern-file
char           *patt_name      = "/sdcard/andar/patt.hiro";
int		cur_marker_id = -1;
int             patt_id;
double          patt_width     = 80.0;
double          patt_center[2] = {0.0, 0.0};
double          patt_trans[3][4];
//the opengl para matrix
extern float   gl_cpara[16];
float gl_para[16];



/*
 * Class:     edu_dhbw_andar_MarkerInfo
 * Method:    artoolkit_init
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_artoolkit_1init
  (JNIEnv *env, jobject object, jint imageWidth, jint imageHeight, jint screenWidth, jint screenHeight) {
    ARParam  wparam;
	
    xsize = imageHeight;
    ysize = imageHeight;
    printf("Image size (x,y) = (%d,%d)\n", xsize, ysize);

    /* set the initial camera parameters */
    if( arParamLoad(cparam_name, 1, &wparam) < 0 ) {
	__android_log_write(ANDROID_LOG_ERROR,"AR","Camera parameter load error !!");
        printf("Camera parameter load error !!\n");
        exit(EXIT_FAILURE);
    }
#ifdef DEBUG_LOGGING
    else {
        __android_log_write(ANDROID_LOG_INFO,"AR","Camera parameter loaded successfully !!");
    }
#endif
    arParamChangeSize( &wparam, imageWidth, imageHeight, &cparam );
    arInitCparam( &cparam );
    printf("*** Camera Parameter ***\n");
    arParamDisp( &cparam );

    if( (patt_id=arLoadPatt(patt_name)) < 0 ) {
	__android_log_write(ANDROID_LOG_ERROR,"AR","pattern load error !!");
        printf("pattern load error !!\n");
        exit(EXIT_FAILURE);
    } 
#ifdef DEBUG_LOGGING
    else {
	__android_log_print(ANDROID_LOG_INFO,"AR","pattern loaded successfully!! id:%d", patt_id);
    }
#endif
    //initialize openGL stuff
    argInit( &cparam, 1.0, 0, screenWidth, screenHeight, 0 );
}

/*
 * Class:     edu_dhbw_andar_MarkerInfo
 * Method:    artoolkit_detectmarkers
 * Signature: ([B[D)I
 */
JNIEXPORT jint JNICALL Java_edu_dhbw_andar_ARToolkit_artoolkit_1detectmarkers
  (JNIEnv *env, jobject object, jbyteArray image, jobject transMatMonitor) {
    ARUint8         *dataPtr;
    ARMarkerInfo    *marker_info;
    double 	    *matrixPtr;
    int             marker_num;
    int             j, k;

    /* grab a vide frame */
    dataPtr = (*env)->GetByteArrayElements(env, image, JNI_FALSE);
    if( count == 0 ) arUtilTimerReset();
    count++;

    /* detect the markers in the video frame */
    if( arDetectMarker(dataPtr, thresh, &marker_info, &marker_num) < 0 ) {
	__android_log_write(ANDROID_LOG_ERROR,"AR","arDetectMarker failed!!");
        exit(EXIT_FAILURE);
    }
#ifdef DEBUG_LOGGING
   __android_log_print(ANDROID_LOG_INFO,"AR","detected %d markers",marker_num);
#endif

    /* check for object visibility */
    k = -1;
    for( j = 0; j < marker_num; j++ ) {
        if( patt_id == marker_info[j].id ) {
            if( k == -1 ) k = j;
            else if( marker_info[k].cf < marker_info[j].cf ) k = j;
        }
    }
    

    /* get the transformation between the marker and the real camera */
    arGetTransMat(&marker_info[k], patt_center, patt_width, patt_trans);

    //lock the matrix
    (*env)->MonitorEnter(env, transMatMonitor);
    cur_marker_id = k;
    argConvGlpara(patt_trans, gl_para);
    (*env)->MonitorExit(env, transMatMonitor);

    (*env)->ReleaseByteArrayElements(env, image, dataPtr, 0); 
    return k;
}
const float box[] =  {
			// FRONT
			-25.0f, -25.0f,  25.0f,
			 25.0f, -25.0f,  25.0f,
			-25.0f,  25.0f,  25.0f,
			 25.0f,  25.0f,  25.0f,
			// BACK
			-25.0f, -25.0f, -25.0f,
			-25.0f,  25.0f, -25.0f,
			 25.0f, -25.0f, -25.0f,
			 25.0f,  25.0f, -25.0f,
			// LEFT
			-25.0f, -25.0f,  25.0f,
			-25.0f,  25.0f,  25.0f,
			-25.0f, -25.0f, -25.0f,
			-25.0f,  25.0f, -25.0f,
			// RIGHT
			 25.0f, -25.0f, -25.0f,
			 25.0f,  25.0f, -25.0f,
			 25.0f, -25.0f,  25.0f,
			 25.0f,  25.0f,  25.0f,
			// TOP
			-25.0f,  25.0f,  25.0f,
			 25.0f,  25.0f,  25.0f,
			 -25.0f,  25.0f, -25.0f,
			 25.0f,  25.0f, -25.0f,
			// BOTTOM
			-25.0f, -25.0f,  25.0f,
			-25.0f, -25.0f, -25.0f,
			 25.0f, -25.0f,  25.0f,
			 25.0f, -25.0f, -25.0f,
		};
const float normals[] =  {
			// FRONT
			0.0f, 0.0f,  1.0f,
			0.0f, 0.0f,  1.0f,
			0.0f, 0.0f,  1.0f,
			0.0f, 0.0f,  1.0f,
			// BACK
			0.0f, 0.0f,  -1.0f,
			0.0f, 0.0f,  -1.0f,
			0.0f, 0.0f,  -1.0f,
			0.0f, 0.0f,  -1.0f,
			// LEFT
			-1.0f, 0.0f,  0.0f,
			-1.0f, 0.0f,  0.0f,
			-1.0f, 0.0f,  0.0f,
			-1.0f, 0.0f,  0.0f,
			// RIGHT
			1.0f, 0.0f,  0.0f,
			1.0f, 0.0f,  0.0f,
			1.0f, 0.0f,  0.0f,
			1.0f, 0.0f,  0.0f,
			// TOP
			0.0f, 1.0f,  0.0f,
			0.0f, 1.0f,  0.0f,
			0.0f, 1.0f,  0.0f,
			0.0f, 1.0f,  0.0f,
			// BOTTOM
			0.0f, -1.0f,  0.0f,
			0.0f, -1.0f,  0.0f,
			0.0f, -1.0f,  0.0f,
			0.0f, -1.0f,  0.0f,
		};
//Lighting variables
const static GLfloat   mat_ambient[]     = {0.0, 0.0, 1.0, 1.0};
const static GLfloat   mat_flash[]       = {0.0, 0.0, 1.0, 1.0};
const static GLfloat   mat_flash_shiny[] = {50.0};
const static GLfloat   light_position[]  = {100.0,-200.0,200.0,0.0};
const static GLfloat   ambi[]            = {0.1, 0.1, 0.1, 0.1};
const static GLfloat   lightZeroColor[]  = {0.9, 0.9, 0.9, 0.1};
/*
 * Class:     edu_dhbw_andar_MarkerInfo
 * Method:    draw
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_draw
  (JNIEnv *env, jobject object) {

  if(cur_marker_id != -1) {
    //setup the 3D environment
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    glMatrixMode(GL_PROJECTION);
    glLoadMatrixf( gl_cpara );
    glClearDepthf( 1.0 );
    glClear(GL_DEPTH_BUFFER_BIT);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    glMatrixMode(GL_MODELVIEW);
    glLoadMatrixf( gl_para );


    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glLightfv(GL_LIGHT0, GL_POSITION, light_position);
    glLightfv(GL_LIGHT0, GL_AMBIENT, ambi);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, lightZeroColor);
    glMaterialfv(GL_FRONT, GL_SPECULAR, mat_flash);
    glMaterialfv(GL_FRONT, GL_SHININESS, mat_flash_shiny);	
    glMaterialfv(GL_FRONT, GL_AMBIENT, mat_ambient);

    //draw cube
    glColor4f(0, 1.0f, 0, 1.0f);
    glTranslatef( 0.0, 0.0, 12.5 );
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisable(GL_TEXTURE_2D);
    glVertexPointer(3, GL_FLOAT, 0, box);
    glNormalPointer(GL_FLOAT,0, normals);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);
    glDrawArrays(GL_TRIANGLE_STRIP, 8, 4);
    glDrawArrays(GL_TRIANGLE_STRIP, 12, 4);
    glDrawArrays(GL_TRIANGLE_STRIP, 16, 4);
    glDrawArrays(GL_TRIANGLE_STRIP, 20, 4);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
  }
}


