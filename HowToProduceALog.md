# Introduction #

how to provide the output of adb logcat, big thx to suzyque!

# Details #

to get logcat, install the Android SDK (http://developer.android.com/sdk/index.html
). You can ignore all the instructions about installing the Eclipse plugin. Once you
have the SDK installed, connect your phone to the computer with the SDK. In a command
prompt window go to your Android SDK tools directory (for me, it is C:\Documents and
Settings\Administrator\My Documents\mobileAR\android\_sdk\_install\android-sdk-windows\tools, but it will depend on where you told the SDK install to unpack the SDK):

./adb logcat -v time >logcat.out

This will start the capture of logcat information. Then run AndAR, after you run
AndAR, Ctrl-C in the command prompt window to stop logcat, then post logcat.out in
this assue as a file attachment.
