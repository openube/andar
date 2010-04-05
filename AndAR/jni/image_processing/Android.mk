#color conversion lib
LOCAL_PATH := $(call my-dir)
$(info $(LOCAL_PATH))

include $(CLEAR_VARS)

LOCAL_MODULE    := yuv420sp2rgb
LOCAL_SRC_FILES := yuv420sp2rgb.c

include $(BUILD_SHARED_LIBRARY)

