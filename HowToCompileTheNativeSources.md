# Introduction #

The AndAR project contains some native C sources. This page describes how to compile those.


# Details #
## Since NDK `r4` ##
  1. Install the android [NDK](http://developer.android.com/sdk/ndk/index.html)
  1. (windows only) Install [Cygwin](http://www.cygwin.com/)
  1. Open the Cygwin shell or some other shell on a linux system
  1. Go to the project's folder
  1. Go to the NDK folder, e.g. `cd /cygdrive/c/Users/Tobi/workspace/AndAR/jni`
  1. Invoke the program called ndk-build(found inside the unzipped NDK folder), e.g. `/opt/android-ndk-r4/ndk-build`
  1. Refesh your project in Eclipse (right-click -> refresh)
  1. If everything went smoothly your libs/armeabi folder should contain `.so` files

## Before NDK `r4` ##

  1. Install the android [NDK](http://developer.android.com/sdk/ndk/index.html)
  1. (windows only) Install [Cygwin](http://www.cygwin.com/)
  1. Open the Cygwin shell or some other shell on a linux system
  1. Go to the NDK folder.
    * This is where you unzipped the android NDK.
    * e.g. you unzipped it to C:\cygwin do a `cd /android-ndk`.
  1. Create a symlink from your eclipse project folder (should be called AndOpenGLCam) to the ndk apps folder, e.g. `ln -s /cygdrive/c/Users/Tobi/workspace/AndAR/ apps/AndAR` (adapt the path)
  1. In the root of the NDK folder run `make APP=AndOpenGLCam`
  1. Refesh your project in Eclipse (right-click -> refresh)
  1. If everything went smoothly your libs/armeabi folder should contain `.so` files