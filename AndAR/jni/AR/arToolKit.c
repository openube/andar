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
 * This is the glue between the Java and the C part of AndAR.
 */

//IMPORTS
#include <GLES/gl.h>
#include <stdio.h>
//ar.h containing the logging switch (DEBUG_LOGGING)
#include <AR/ar.h>
#include <AR/param.h>
#include <../marker_info.h>
#include <android/log.h>
#include <stdlib.h>
#include <simclist.h>
#include <string.h>
//END IMPORTS

//DATASTRUCTURES
/**
 * Represents an AR Object. There is one Java object that corresponds to each of those.
 * The data structure will contain all information that is needed in the native code.
 */
typedef struct {
    int       name;
    int        id;
//    double     marker_coord[4][2];//not needed anymore -> using the array of the corresponding java object
//    double     trans[3][4];//not needed anymore -> using the array of the corresponding java object
    int contF;
    double     marker_width;
    double     marker_center[2];
	jobject objref;
} Object;

/**
 * Data structure representing a pattern ID. Used for caching the pattern IDs.
 * Contains the filename of the pattern file, e.g. patt.hiro, and the the according ID.
 */
typedef struct {
	int id;
	char* filename;
} patternID;

//size of the camera images
int             xsize, ysize;
//Binarization threshold
int             thresh = 100;
int             count = 0;
//camera distortion parameters
ARParam         cparam;

//the opengl para matrix
extern float   gl_cpara[16];
//A list of AR objects
list_t objects;
//A list of cached pattern IDs
list_t* patternIDs = NULL;

//END DATASTRUCTURES

/*
 * compare an AR object with an ID
 * used to search the list of objects for an particular object.
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

/**
 * This function will search the list of cached pattern IDs for a pattern ID according to the given filename.
 * If it finds one, the found ID will be returned.
 * If not, -1 will be returned.
 */
int getPatternIDFromList(const char *filename) {
	int id = -1;
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","trying to retrieve pattern from list");
#endif
	if(patternIDs == NULL) {
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","list of patterns is null!!");
#endif	
		return -1;
	}
	list_iterator_start(patternIDs);
	while (list_iterator_hasnext(patternIDs)) { 
		patternID * currPatt = (patternID *)list_iterator_next(patternIDs);
#ifdef DEBUG_LOGGING
		__android_log_print(ANDROID_LOG_INFO,"AR native","current pattern fiel: %s",currPatt->filename);
#endif
		if(strcmp(filename, currPatt->filename)==0) {
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","found pattern in list");
#endif
			id = currPatt->id;
			break;
		}
	}
	list_iterator_stop(patternIDs); 
	
#ifdef DEBUG_LOGGING
		if(id==-1)
		__android_log_print(ANDROID_LOG_INFO,"AR native","found no pattern in the list for file %s",filename);
#endif
	return id;
}

/**
 * Do some basic initialization, like creating data structures.
 */
/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    artoolkit_init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_artoolkit_1init__
  (JNIEnv * env, jobject object) {
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","initializing artoolkit");
#endif
	//initialize the list of objects
	list_init(&objects);
	//set the comperator function:
	list_attributes_comparator(&objects, objectcomparator);
	//Intialize the list of pattern IDs
	//It might already be initialized, as the native library doesn't get unloaded after the java application finished
	//The pattern IDs will be cached during multiple invocation of the Java application(AndAR)
	if(patternIDs==NULL) {
		patternIDs = (list_t*) malloc(sizeof(list_t));
		if(patternIDs == NULL) {
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","list of patterns is null after init!!");
#endif	
		} else {
			list_init(patternIDs);
		}
	}	
#ifdef DEBUG_LOGGING
		__android_log_write(ANDROID_LOG_INFO,"AR native","finished initializing ARToolkit");
#endif
  }

/**
 * Register a object to the native library. From now on the detection function will determine
 * if the given object is visible on a marker, and set the transformation matrix accordingly.
 * @param id a unique ID of the object
 * @param patternName the fileName of the pattern
 * @param markerWidth the width of the object
 * @param markerCenter the center of the object
 */  
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
		//ok object allocated, now fill the struct with data
#ifdef DEBUG_LOGGING
		__android_log_print(ANDROID_LOG_INFO,"AR native","registering object with name %d", name);
#endif
		newObject->name = (int) name;
		newObject->marker_width = (double) width;
		newObject->contF = 0;
		newObject->marker_center[0] = (double) centerArr[0];
		newObject->marker_center[1] = (double) centerArr[1];
		newObject->objref = (*env)->NewGlobalRef(env, obj);
		//search the list of pattern IDs for a pattern matching the given filename
		//this is needed as the native library doesn't get unloaded after the Java application finished
		//and multiple invocations of arLoadPatt with the same pattern file will result in incorrect IDs
		if( (newObject->id = getPatternIDFromList(cPatternFile)) < 0 ) {
			if( (newObject->id = arLoadPatt(cPatternFile)) < 0 ) {
				//failed to read the pattern file
				//release the object and throw an exception
				free(newObject);
				jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
				if ( exc != NULL ) 
					(*env)->ThrowNew( env, exc, "could not read pattern file for object." );
			} else {
	#ifdef DEBUG_LOGGING
			__android_log_print(ANDROID_LOG_INFO,"AR native","loaded marker with ID %d from file: %s", newObject->id, cPatternFile);
	#endif
				//add object to the list
				list_append(&objects, newObject);
				patternID* newPatternID = (patternID *)malloc(sizeof(patternID));
				if(newPatternID != NULL) {
					newPatternID->filename = strdup(cPatternFile);
					newPatternID->id = newObject->id;
					list_append(patternIDs, newPatternID);
				}
			}
		} else {
#ifdef DEBUG_LOGGING
			__android_log_print(ANDROID_LOG_INFO,"AR native","loaded marker with ID %d from cached pattern ID", newObject->id, cPatternFile);
	#endif
				//add object to the list
				list_append(&objects, newObject);
		}
	}
	//release the marker center array
	(*env)->ReleaseDoubleArrayElements(env, center, centerArr, 0);
	(*env)->ReleaseStringUTFChars( env, patternFile, cPatternFile);
  }
  
/**
 * Remove the object from the list of registered objects.
 * @param id the id of the object.
 */
/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    removeObject
 * Signature: (I)V
 */
 //TODO release globalref
JNIEXPORT void JNICALL Java_edu_dhbw_andar_ARToolkit_removeObject
  (JNIEnv *env, jobject artoolkit, jint objectID) {
	if(list_delete(&objects,&objectID) != 0) {
		//failed to delete -> throw error
		jclass exc = (*env)->FindClass( env, "edu/dhbw/andar/exceptions/AndARException" );  
		if ( exc != NULL ) 
			(*env)->ThrowNew( env, exc, "could not delete object from native array" );
	}
#ifdef DEBUG_LOGGING
	__android_log_write(ANDROID_LOG_INFO,"AR native","array of objects still containing the following elements:");
	list_iterator_start(&objects);        /* starting an iteration "session" */
    while (list_iterator_hasnext(&objects)) { /* tell whether more values available */
        Object* curObject = (Object *)list_iterator_next(&objects);     /* get the next value */
		__android_log_print(ANDROID_LOG_INFO,"AR native","name: %s", curObject->name);
		__android_log_print(ANDROID_LOG_INFO,"AR native","id: %s", curObject->id);
	}
    list_iterator_stop(&objects);
#endif
}

/**
 * Do initialization specific to the image/screen dimensions.
 * @param imageWidth width of the image data
 * @param imageHeight height of the image data
 * @param screenWidth width of the screen
 * @param screenHeight height of the screen
 */
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

/**
 * detect the markers in the given frame.
 * @param in the image 
 * @param matrix the transformation matrix for each marker, will be locked right before the trans matrix will be altered
 * @return number of markers
 */
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
#ifdef DEBUG_LOGGING
			__android_log_print(ANDROID_LOG_INFO,"AR native","marker with id: %d", marker_info[j].id);
#endif
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
			curObject->contF = 0;
			(*env)->SetBooleanField(env, curObject->objref, visibleField, JNI_FALSE);
#ifdef DEBUG_LOGGING
			__android_log_print(ANDROID_LOG_INFO,"AR native","object %d  not visible, with marker ID %d",curObject->name,curObject->id);
#endif
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
		if( curObject->contF == 0 ) {
			arGetTransMat(&marker_info[k], curObject->marker_center, curObject->marker_width, transMat);
		} else {
			arGetTransMatCont(&marker_info[k], transMat, curObject->marker_center, curObject->marker_width, transMat);
		}
		curObject->contF = 1;
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
    return marker_num;
}


/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    arUtilMatInv
 * Signature: ([D[D)I
 */
JNIEXPORT jint JNICALL Java_edu_dhbw_andar_ARToolkit_arUtilMatInv
  (JNIEnv *env, jclass this, jdoubleArray mat1, jdoubleArray mat2) {
	double 	    *mat1Ptr;
	double 	    *mat2Ptr;
	int retval;
	mat1Ptr = (*env)->GetDoubleArrayElements(env, mat1, JNI_FALSE);
	mat2Ptr = (*env)->GetDoubleArrayElements(env, mat2, JNI_FALSE);
	retval = arUtilMatInv(mat1Ptr,mat2Ptr);
	(*env)->ReleaseDoubleArrayElements(env, mat1, mat1Ptr, 0);
	(*env)->ReleaseDoubleArrayElements(env, mat2, mat2Ptr, 0);
	return retval;
  }

/*
 * Class:     edu_dhbw_andar_ARToolkit
 * Method:    arUtilMatMul
 * Signature: ([D[D[D)I
 */
JNIEXPORT jint JNICALL Java_edu_dhbw_andar_ARToolkit_arUtilMatMul
  (JNIEnv *env, jclass this, jdoubleArray mat1, jdoubleArray mat2, jdoubleArray result) {
	double 	    *mat1Ptr;
	double 	    *mat2Ptr;
	double 	    *resPtr;
	int retval;
	mat1Ptr = (*env)->GetDoubleArrayElements(env, mat1, JNI_FALSE);
	mat2Ptr = (*env)->GetDoubleArrayElements(env, mat2, JNI_FALSE);
	resPtr = (*env)->GetDoubleArrayElements(env, result, JNI_FALSE);
	retval = arUtilMatMul(mat1Ptr,mat2Ptr,resPtr);
	(*env)->ReleaseDoubleArrayElements(env, mat1, mat1Ptr, 0);
	(*env)->ReleaseDoubleArrayElements(env, mat2, mat2Ptr, 0);
	(*env)->ReleaseDoubleArrayElements(env, result, resPtr, 0);
	return retval;
  }


