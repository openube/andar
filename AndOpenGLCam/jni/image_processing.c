/**
	Copyright (C) 2010  Tobias Domhan

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
#include <jni.h>
#include <preview_handler_jni.h>
#include <image_processing.h>
#include <math.h>
#include <stdlib.h>

//Definitions:
#define BLACK 0
#define WHITE 255
#define PI 3.14
#define HOR_GRAD 1
#define VERT_GRAD 2
#define C45_GRAD 3
#define C135_GRAD 4


/*
 * Class:     edu_dhbw_andopenglcam_CameraPreviewHandler
 * Method:    binarize
 * Signature: ([BII[BI)V
 * binarize a image
 * @param in the input array
 * @param width image width
 * @param height image height
 * @param out the output array
 * @param threshold the binarization threshold
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andopenglcam_CameraPreviewHandler_binarize
  (JNIEnv *env, jobject object, jbyteArray inArray, jint width, jint height, jbyteArray outArray, jint threshold) {
	int i,j,ptr=0;
	//a pointer to a jbyte will not work because jbyte is actually a signed char, therefor comparing with the threshold would fail
        unsigned char *in;
        unsigned char *out;
	in = (*env)->GetByteArrayElements(env, inArray, JNI_FALSE);
        out = (*env)->GetByteArrayElements(env, outArray, JNI_FALSE);
	for(i=0;i<height;i++) {
		for(j=0;j<width;j++) {
			out[ptr++]=(in[ptr]<=threshold) ? BLACK : WHITE;
		}
	}
	(*env)->ReleaseByteArrayElements(env, inArray, in, 0);
        (*env)->ReleaseByteArrayElements(env, outArray, out, 0);
}	


/*
 * Class:     edu_dhbw_andopenglcam_CameraPreviewHandler
 * Method:    detect_edges
 * Signature: ([BII[B[F)V
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andopenglcam_CameraPreviewHandler_detect_1edges
  (JNIEnv *env, jobject object, jbyteArray inArray, jint width, jint height, jbyteArray outArray, jint threshold) {
        int i,j,ptr=0,ptrAbove,ptrBelow;
	int x,y;
	double tmp;
        //a pointer to a jbyte will not work because jbyte is actually a signed char, therefor comparing with the threshold would fail
        unsigned char *in;
        unsigned char *out;
	//gradient(angle):
        static short *grad;
	//magnitude
        static double *mag;
	//initialize temporary arrays:(we will do this only once, if the width/height changes we will have a poblem)
	if(grad == NULL) {
		 grad = (short*)malloc(width*height*sizeof(short));
	}
	if(mag == NULL) {
		 mag = (double*)malloc(width*height*sizeof(double));
	}
        in = (*env)->GetByteArrayElements(env, inArray, JNI_FALSE);
        out = (*env)->GetByteArrayElements(env, outArray, JNI_FALSE);
	//calculate magnitude and angle of the edge for each pixel(sobel operator)
        for(i=0;i<height;i++) {
                for(j=0;j<width;j++) {
			if(i==0 || j==0 || i==height-1 || j==width-1) {
				//erase border:
				out[ptr] = BLACK;
				grad[ptr] = BLACK;
				mag[ptr] = BLACK;
			} else {
				ptrAbove=ptr-width;
				ptrBelow=ptr+width;
				x=2*in[ptr-1]-2*in[ptr+1]+in[ptrAbove-1]-in[ptrAbove+1]-in[ptrBelow-1]+in[ptrBelow+1];	
				y=in[ptrAbove-1]+2*in[ptrAbove]+in[ptrAbove+1]-in[ptrBelow-1]-2*in[ptrBelow]-in[ptrBelow+1];
				//x/=8.0;//scale not needed as we will threshold the result
				//y/=8.0;
				mag[ptr] = sqrt(x*x+y*y);
				/*tmp = atan2(y,x) * 180.0 / PI - 90.0;	
				//we don't care for the exact angle
				if((tmp>=0 && tmp<=22.5)||(tmp>=157.5 && tmp<=202.5)||(tmp>=337.5&&tmp<=361)) {
					grad[ptr]=HOR_GRAD;		
				} else if((tmp>=22.5 && tmp<=67.5)||(tmp>=202.5 && tmp<=247.5)) {
                                        grad[ptr]=C45_GRAD;
                                } else if((tmp>=67.5 && tmp<=112.5)||(tmp>=247.5 && tmp<=292.5)) {
                                        grad[ptr]=VERT_GRAD;
                                } else if((tmp>=112.5 && tmp<=137.5)||(tmp>=292.5 && tmp<=337.5)) {
                                        grad[ptr]=C135_GRAD;
                                }*/ 
				tmp = atan2(y,x)-1.57;
                                //we don't care for the exact angle
                                if((tmp>=0 && tmp<=0.392699)||(tmp>=2.74889 && tmp<=3.53429)||(tmp>=5.89049&&tmp<=6.3)) {
                                        grad[ptr]=HOR_GRAD;
                                } else if((tmp>=0.392699 && tmp<=1.1781)||(tmp>=3.53429 && tmp<=4.31969)) {
                                        grad[ptr]=C45_GRAD;
                                } else if((tmp>=1.1781 && tmp<=1.9635)||(tmp>=4.31969 && tmp<=5.10509)) {
                                        grad[ptr]=VERT_GRAD;
                                } else if((tmp>=1.9635 && tmp<=2.74889)||(tmp>=5.10509 && tmp<=5.89049)) {
                                        grad[ptr]=C135_GRAD;
                                }
				//out[ptr]=(tmp>255) ? 255 : tmp;
				//mag[ptr]=sqrt(x*x+y*y)/5.6;
				//bin:
				//mag[ptr] = (mag[ptr]<=50) ? BLACK : WHITE;
                                //grad[ptr]=(x==0) ? 1.57 : atan(y/x);
			}	
			ptr++;
                }
        }
	
	ptr=0;
	for(i=1;i<height-1;i++) {
                for(j=1;j<width-1;j++) {	
			if(mag[ptr] > threshold) {
				switch(grad[ptr]) {
				case VERT_GRAD:
					out[ptr]=(mag[ptr]>mag[ptr-1]&&mag[ptr]>mag[ptr+1]) ? WHITE : BLACK;
					break;
				case HOR_GRAD:
					out[ptr]=(mag[ptr]>mag[ptr-width]&&mag[ptr]>mag[ptr+width]) ? WHITE : BLACK;
		                        break;
				case C135_GRAD:
					out[ptr]=(mag[ptr]>mag[ptr-width+1]&&mag[ptr]>mag[ptr+width-1]) ? WHITE : BLACK;
        	        	        break;
				case C45_GRAD:
					out[ptr]=(mag[ptr]>mag[ptr-width-1]&&mag[ptr]>mag[ptr+width+1]) ? WHITE : BLACK;
	        	                break;
				default:
					out[ptr]=BLACK;
					break;
				}
			} else {
				out[ptr]=BLACK;
			}
			ptr++;
		}	
	}
        (*env)->ReleaseByteArrayElements(env, inArray, in, 0);
        (*env)->ReleaseByteArrayElements(env, outArray, out, 0);
}

/*
 * Class:     edu_dhbw_andopenglcam_CameraPreviewHandler
 * Method:    detect_edges_bin
 * Signature: ([BII[BI)V
http://homepages.inf.ed.ac.uk/rbf/HIPR2/sobel.htm
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andopenglcam_CameraPreviewHandler_detect_1edges_1simple
  (JNIEnv *env, jobject object, jbyteArray inArray, jint width, jint height, jbyteArray outArray, jint threshold){
        int i,j,ptr=0,ptrAbove,ptrBelow,x,y,tmp;
        //a pointer to a jbyte will not work because jbyte is actually a signed char, therefor comparing with the threshold would fail
        unsigned char *in;
        unsigned char *out;
        in = (*env)->GetByteArrayElements(env, inArray, JNI_FALSE);
        out = (*env)->GetByteArrayElements(env, outArray, JNI_FALSE);

        for(i=0;i<height;i++) {
                for(j=0;j<width;j++) {
                        if(i==0 || j==0 || i==height-1 || j==width-1) {
                                //erase border:
                                out[ptr]=0;
                        } else {
                                ptrAbove=ptr-width;
                                ptrBelow=ptr+width;
                                x=abs(2*in[ptr-1]-2*in[ptr+1]+in[ptrAbove-1]-in[ptrAbove+1]-in[ptrBelow-1]+in[ptrBelow+1]);  
                                y=abs(in[ptrAbove-1]+2*in[ptrAbove]+in[ptrAbove+1]-in[ptrBelow-1]-2*in[ptrBelow]-in[ptrBelow+1]);
                                tmp = x+y;
                                out[ptr]=(tmp>threshold) ? WHITE : BLACK;
                        } 
                        ptr++;
                }
        }
        (*env)->ReleaseByteArrayElements(env, inArray, in, 0);
        (*env)->ReleaseByteArrayElements(env, outArray, out, 0);
}



/*
 * apply filter
 */
inline void filter(double *din, double *dout, unsigned sx, unsigned sy, double *kernel, unsigned kdim) {
    unsigned int x,y,i,j;
    for (y = (kdim/2); y < sy - (kdim/2); ++y) {
        for (x = (kdim/2); x < sx - (kdim/2); ++x)
        {
            dout[y*sx+x] = 0.;
            for (i = 0; i < kdim; ++i) {
                for (j = 0; j < kdim; ++j) {
                    dout[y*sx+x] += kernel[i*kdim+j] * din[(y+j-kdim/2)*sx + x+i-kdim/2];
		}
	    }
        }	
     }
} 
