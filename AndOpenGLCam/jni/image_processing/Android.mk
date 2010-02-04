#color conversion lib
LOCAL_PATH := $(call my-dir)
$(info $(LOCAL_PATH))

include $(CLEAR_VARS)

LOCAL_MODULE    := yuv420sp2rgb
LOCAL_SRC_FILES := yuv420sp2rgb.c

include $(BUILD_SHARED_LIBRARY)

# image processing lib
#
include $(CLEAR_VARS)

LOCAL_MODULE    := imageprocessing
LOCAL_SRC_FILES := image_processing.c

#LOCAL_STATIC_LIBRARIES := libyuv420sp2rgb

include $(BUILD_SHARED_LIBRARY)

