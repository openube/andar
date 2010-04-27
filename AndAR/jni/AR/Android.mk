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

LOCAL_MODULE    := ar
LOCAL_SRC_FILES := simclist.c mAlloc.c mFree.c mAllocDup.c mDup.c mAllocTrans.c mTrans.c mAllocMul.c   mMul.c mAllocInv.c   mInv.c   mSelfInv.c mAllocUnit.c  mUnit.c mDisp.c mDet.c mPCA.c vAlloc.c vDisp.c vFree.c vHouse.c vInnerP.c vTridiag.c paramGet.c paramDecomp.c paramDistortion.c  paramChangeSize.c  paramFile.c paramDisp.c arDetectMarker.c arGetTransMat.c arGetTransMat2.c arGetTransMat3.c arGetTransMatCont.c arLabeling.c arDetectMarker2.c arGetMarkerInfo.c arGetCode.c arUtil.c arToolKit.c arGl.c
#LOCAL_SRC_FILES := arToolKit.c
#arToolKit.c

#LOCAL_STATIC_LIBRARIES := libarfirst libarsecond libarthird

#bind logging library
LOCAL_LDLIBS := -llog #-lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)

#LOCAL_MODULE    := arjni
#LOCAL_SRC_FILES := arToolKit.c
#LOCAL_SRC_FILES := arToolKit.c
#arToolKit.c

#LOCAL_STATIC_LIBRARIES := libarfirst libarsecond libarthird

#include $(BUILD_SHARED_LIBRARY)

 
