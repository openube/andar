# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


#color conversion lib
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/src/include
LOCAL_MODULE    := arplus
LOCAL_SRC_FILES := src/DLL.cpp src/FixedPoint.cpp src/Profiler.cpp src/librpp.cpp src/robust_pose.cpp src/rpp.cpp src/rpp_quintic.cpp src/rpp_svd.cpp src/rpp_vecmat.cpp src/MemoryManager.cpp src/MemoryManagerMemMap.cpp src/CameraAdvImpl.cxx src/CameraFactory.cxx src/CameraImpl.cxx src/core/arBitFieldPattern.cxx src/core/arDetectMarker.cxx src/core/arDetectMarker2.cxx src/core/arGetCode.cxx src/core/arGetMarkerInfo.cxx src/core/arGetTransMat.cxx src/core/arGetTransMat2.cxx src/core/arGetTransMat3.cxx src/core/arGetTransMatCont.cxx src/core/arLabeling.cxx src/core/arMultiActivate.cxx src/core/arMultiGetTransMat.cxx src/core/arMultiReadConfigFile.cxx src/core/arUtil.cxx src/core/byteSwap.cxx src/core/matrix.cxx src/core/mPCA.cxx src/core/paramDecomp.cxx src/core/paramDistortion.cxx src/core/paramFile.cxx src/core/rppGetTransMat.cxx src/core/rppMultiGetTransMat.cxx src/core/vector.cxx src/extra/BCH.cxx src/TrackerImpl.cxx src/TrackerMultiMarkerImpl.cxx src/TrackerSingleMarkerImpl.cxx

#arToolKit.c

#LOCAL_STATIC_LIBRARIES := libarfirst libarsecond libarthird

#bind logging library
#LOCAL_LDLIBS := -llog -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)

#LOCAL_MODULE    := arjni
#LOCAL_SRC_FILES := arToolKit.c
#LOCAL_SRC_FILES := arToolKit.c
#arToolKit.c

#LOCAL_STATIC_LIBRARIES := libarfirst libarsecond libarthird

#include $(BUILD_SHARED_LIBRARY)

 
