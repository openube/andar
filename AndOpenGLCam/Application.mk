#APP_PROJECT_PATH := /home/tobi/Code/workspace-galileo/AndOpenGLCam
APP_PROJECT_PATH := /home/tobi/Code/workspace-galileo/AndOpenGLCam-Subclipse
APP_MODULES      := yuv420sp2rgb imageprocessing ar
#cflags for the t-mobile g1, make break app on other phones
APP_CFLAGS :=  -march=armv6 -mfloat-abi=softfp -mfpu=vfp
#APP_CFLAGS :=  -march=armv6 -mfloat-abi=softfp -mfpu=vfp
#APP_MODULES      := yuv420sp2rgb imageprocessing libarfirst libarsecond libarthird ar
#APP_BUILD_SCRIPT := /home/tobi/Code/workspace-galileo/AndOpenGLCam-Subclipse/jni/Android.mk
