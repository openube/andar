# Introduction #

This page explains the structure of the project's sources.


# Details #

The source code repository contains three Eclipse (Galileo) projects. Those are:
  * AndAR
    * Currently **under development**.
    * Offering a pure Java API to the ARToolkit.
  * AndAR Model Viewer
    * A app to view [wavefront obj](http://en.wikipedia.org/wiki/Obj) 3d models
    * Makes use of the AndAR project.
    * A sample application that shows how to use AndAR.
    * **Not needed** if you want to develop your very **own application** based on AndAR.
  * AndAR Pong
    * An augmented version of Pong.
  * AndOpenGLCam (**deprecated**)
    * My first try to get the ARToolkit working on Android.
    * Should basically work.
    * There is no real API in order to replace the cube with something else, you would have to edit the C source code.
    * Will be **deprecated** as soon as the AndAR project advances.
  * AndObjViewer(deprecated)
    * A app to view [wavefront obj](http://en.wikipedia.org/wiki/Obj) 3d models