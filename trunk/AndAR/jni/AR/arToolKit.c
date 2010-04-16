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
#include <simclist.h>

typedef struct {
    int       name;
    int        id;
    int        visible;
	int		   collide;
    double     marker_coord[4][2];
    double     trans[3][4];
    double     marker_width;
    double     marker_center[2];
	jobject objref;
} Object;

int             xsize, ysize;
int             thresh = 100;
int             count = 0;

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
list_t objects;

/*
 * compare two objects by area
 * return 0 if the objects equal
 * and 1 otherwise
 */
int objectcomparator(const void *a, const void *b) {
	/* compare areas */
	const Object *A = (Object *) a;
	const int *B = (int *) b;
	if(A->name == *B)
		return 0;
	else
		return 1;
}

/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    artoolkit_init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_artoolkit_1init__
  (JNIEnv * env, jobject object) {
	//initialize the list of objects
	list_init(&objects);
	//set the comperator function:
	list_attributes_comparator(&objects, objectcomparator);
  }
  
/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    addObject
 * Signature: (ILedu/dhbw/andar/ARObject;Ljava/lang/String;D[D)V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_addObject
  (JNIEnv * env, jobject artoolkit, jint name, jobject obj, jstring patternFile, jdouble width, jdoubleArray center) {
	Object* newObject;
	jdouble* centerArr;
	centerArr = (*env)->GetDoubleArrayElements(env, center, NULL);
	const char *cPatternFile = (*env)->GetStringUTFChars( env, patternFile, NULL ); 
	if (centerArr == NULL) {
		//could not retrieve the java array
		jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "could not retrieve array of the marker center in native code." ); 
	}
	if((newObject = (Object *)malloc(sizeof(Object))) == NULL) {
		//something went wrong with allocating -> throw error
		jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "could not allocate memory for new object." ); 
	} else {
		//ok object allocate, now fill the struct with data
#ifdef DEBUG_LOGGING
		__android_log_print(ANDROID_LOG_INFO,"AR native","registering object with name %d", name);
#endif
		newObject->name = (int) name;
		newObject->marker_width = (double) width;
		newObject->marker_center[0] = (double) centerArr[0];
		newObject->marker_center[1] = (double) centerArr[1];
		newObject->objref = (*env)->NewGlobalRef(env, obj);
		if( (newObject->id = arLoadPatt(cPatternFile)) < 0 ) {
			//failed to read the pattern file
			//release the object and throw an exception
			free(newObject);
			jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
			if ( exc != NULL ) 
				(*env)->ThrowNew( env, exc, "could not read pattern file for object." );
		}
		//add object to the list
		list_append(&objects, newObject);
	}
	//release the marker center array
	(*env)->ReleaseDoubleArrayElements(env, center, centerArr, 0);
	(*env)->ReleaseStringUTFChars( env, patternFile, cPatternFile);
  }
  

/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    removeObject
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_removeObject
  (JNIEnv *env, jobject artoolkit, jint objectID) {
	list_delete(&objects,&objectID);
  }

/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    artoolkit_init
 * Signature: (Ljava/lang/String;IIII)V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_artoolkit_1init__Ljava_lang_String_2IIII
  (JNIEnv *env, jobject object, jstring calibFile, jint imageWidth, jint imageHeight, jint screenWidth, jint screenHeight) {
    ARParam  wparam;
	const char *cparam_name = (*env)->GetStringUTFChars( env, calibFile, NULL ); 

	
    xsize = imageHeight;
    ysize = imageHeight;
    printf("Image size (x,y) = (%d,%d)\n", xsize, ysize);

    /* set the initial camera parameters */
    if( arParamLoad(cparam_name, 1, &wparam) < 0 ) {
	__android_log_write(ANDROID_LOG_ERROR,"AR native","Camera parameter load error !!");
	    jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARRuntimeException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "Camera parameter load error !!" ); 
        //exit(EXIT_FAILURE);
    }
#ifdef DEBUG_LOGGING
    else {
        __android_log_write(ANDROID_LOG_INFO,"AR native","Camera parameter loaded successfully !!");
    }
#endif
    arParamChangeSize( &wparam, imageWidth, imageHeight, &cparam );
    arInitCparam( &cparam );
    printf("*** Camera Parameter ***\n");
    arParamDisp( &cparam );

    if( (patt_id=arLoadPatt(patt_name)) < 0 ) {
	//throw runtime exception as this method may be called by a background thread
	__android_log_write(ANDROID_LOG_ERROR,"AR native","pattern load error !!");
        	    jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARRuntimeException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "pattern load error !!" ); 
    } 
#ifdef DEBUG_LOGGING
    else {
	__android_log_print(ANDROID_LOG_INFO,"AR native","pattern loaded successfully!! id:%d", patt_id);
    }
#endif
    //initialize openGL stuff
    argInit( &cparam, 1.0, 0, screenWidth, screenHeight, 0 );
	
	//gl_cpara
	jclass arObjectClass = (*env)->FindClass(env, "edu/dhbw/andar/ARObject");
	if (arObjectClass != NULL) {
		jfieldID glCameraMatrixFieldID = (*env)->GetStaticFieldID(env, arObjectClass, "glCameraMatrix", "[F");
		if (glCameraMatrixFieldID != NULL) {
			jobject glCameraMatrixObj = (*env)->GetStaticObjectField(env, arObjectClass, glCameraMatrixFieldID);
			if(glCameraMatrixObj != NULL) {
				float *glCamMatrix = (*env)->GetFloatArrayElements(env, glCameraMatrixObj, JNI_FALSE);
				int i=0;
				for(i=0;i<16;i++)
					glCamMatrix[i] = gl_cpara[i];
				(*env)->ReleaseFloatArrayElements(env, glCameraMatrixObj, glCamMatrix, 0); 
			}
		}
	}
	
	(*env)->ReleaseStringUTFChars( env, calibFile, cparam_name);
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
    int             j, k=-1;
	Object* curObject;

    /* grab a vide frame */
    dataPtr = (*env)->GetByteArrayElements(env, image, JNI_FALSE);
    if( count == 0 ) arUtilTimerReset();
    count++;

    /* detect the markers in the video frame */
    if( arDetectMarker(dataPtr, thresh, &marker_info, &marker_num) < 0 ) {
	__android_log_write(ANDROID_LOG_ERROR,"AR native","arDetectMarker failed!!");
		jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "failed to detect marker" ); 
    }
#ifdef DEBUG_LOGGING
   __android_log_print(ANDROID_LOG_INFO,"AR native","detected %d markers",marker_num);
#endif


    //lock the matrix
    /*(*env)->MonitorEnter(env, transMatMonitor);
    cur_marker_id = k;
    argConvGlpara(patt_trans, gl_para);
    (*env)->MonitorExit(env, transMatMonitor);*/
	
	static jfieldID visibleField = NULL;
	static jfieldID glMatrixField = NULL;
	static jfieldID transMatField = NULL;
	jclass arObjectClass = NULL;
	jfloatArray glMatrixArrayObj = NULL;
	jdoubleArray transMatArrayObj = NULL;

#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","done detecting markers, going to iterate over markers now");
#endif
	//iterate over objects:
	list_iterator_start(&objects);        /* starting an iteration "session" */
	int itCount = 0;
    while (list_iterator_hasnext(&objects)) { /* tell whether more values available */
        curObject = (Object *)list_iterator_next(&objects);     /* get the next value */
#ifdef DEBUG_LOGGING
		__android_log_print(ANDROID_LOG_INFO,"AR native","now handling object with id %d, in %d iteration",curObject->name, itCount);
#endif
		itCount++;
		// //get field ID'		
		if(visibleField == NULL) {
			if(arObjectClass == NULL) {
				if(curObject->objref != NULL)
					arObjectClass = (*env)->GetObjectClass(env, curObject->objref);
			}
			if(arObjectClass != NULL) {
				visibleField = (*env)->GetFieldID(env, arObjectClass, "visible", "Z");//Z means boolean
			}
		}
		if(glMatrixField == NULL) {
			if(arObjectClass == NULL) {
				if(curObject->objref != NULL)
					arObjectClass = (*env)->GetObjectClass(env, curObject->objref);
			}
			if(arObjectClass != NULL) {
				glMatrixField = (*env)->GetFieldID(env, arObjectClass, "glMatrix", "[F");//[F means array of floats
			}
		}

		if(transMatField == NULL) {
			if(arObjectClass == NULL) {
				if(curObject->objref != NULL)
					arObjectClass = (*env)->GetObjectClass(env, curObject->objref);
			}
			if(arObjectClass != NULL) {
				transMatField = (*env)->GetFieldID(env, arObjectClass, "transMat", "[D");//[D means array of doubles
			}
		}

		if(visibleField == NULL || glMatrixField == NULL || transMatField == NULL) {
			//something went wrong..
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","error: either visibleField or glMatrixField or transMatField null");
#endif
			continue;
		}
		
		 // check for object visibility 
		k = -1;
		for( j = 0; j < marker_num; j++ ) {
			if( curObject->id == marker_info[j].id ) {
				if( k == -1 ) {
					k = j;
#ifdef DEBUG_LOGGING
					__android_log_print(ANDROID_LOG_INFO,"AR native","detected object %d with marker %d and object marker %d",curObject->name,k,curObject->id);
#endif
				}
				else if( marker_info[k].cf < marker_info[j].cf )  {
#ifdef DEBUG_LOGGING
					__android_log_print(ANDROID_LOG_INFO,"AR native","detected better object %d with marker %d and object marker %d",curObject->name,k,curObject->id);
#endif
					k = j;
				}
			}
		}
		if( k == -1 ) {
			//object not visible
			(*env)->SetBooleanField(env, curObject->objref, visibleField, JNI_FALSE);
			continue;
		}
		//object visible
		
		//lock the object
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","locking object");
#endif
		(*env)->MonitorEnter(env, curObject->objref);
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","done locking object...obtaining arrays");
#endif
		//access the arrays of the current object
		glMatrixArrayObj = (*env)->GetObjectField(env, curObject->objref, glMatrixField);
		transMatArrayObj = (*env)->GetObjectField(env, curObject->objref, transMatField);
		if(transMatArrayObj == NULL || glMatrixArrayObj == NULL) {
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","failed to fetch the matrix arrays objects");
#endif
			continue;//something went wrong
		}
		float *glMatrix = (*env)->GetFloatArrayElements(env, glMatrixArrayObj, JNI_FALSE);
		if(glMatrix == NULL ) {
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","failed to fetch the matrix arrays");
#endif
			continue;//something went wrong
		}
		double* transMat = (*env)->GetDoubleArrayElements(env, transMatArrayObj, JNI_FALSE);
		if(transMat == NULL) {
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","failed to fetch the matrix arrays");
#endif
			continue;//something went wrong
		}
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","calculating trans mat now");
#endif
		// get the transformation between the marker and the real camera 
		//arGetTransMat(&marker_info[k], curObject->marker_center, curObject->marker_width, curObject->trans);
		arGetTransMat(&marker_info[k], curObject->marker_center, curObject->marker_width, transMat);
		//arGetTransMat(&marker_info[k], curObject->marker_center, curObject->marker_width, patt_trans);
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","calculating OpenGL trans mat now");
#endif
		argConvGlpara(transMat, glMatrix);
		//argConvGlpara(patt_trans, gl_para);
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","releasing arrays");
#endif
		(*env)->ReleaseFloatArrayElements(env, glMatrixArrayObj, glMatrix, 0); 
		(*env)->ReleaseDoubleArrayElements(env, transMatArrayObj, transMat, 0); 
		
		(*env)->SetBooleanField(env, curObject->objref, visibleField, JNI_TRUE);
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","releasing lock");
#endif		
		//release the lock on the object
		(*env)->MonitorExit(env, curObject->objref);
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","done releasing lock");
#endif
    }
    list_iterator_stop(&objects);         /* starting the iteration "session" */
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","releasing image array");
#endif
    (*env)->ReleaseByteArrayElements(env, image, dataPtr, 0); 
#ifdef DEBUG_LOGGING
        __android_log_write(ANDROID_LOG_INFO,"AR native","releasing image array");
#endif
    return k;
}


