# Introduction #

This page describes, how you can use your very own AR markers. (for developers)


# Details #

For each marker there is a marker file. This file contains information, about how the marker looks. This information is used by the ARToolkit in order to distinguish different markers. All marker file have to be placed in the assets folder of the eclipse project. The name of this file has to be passed to the ARObject's constructor.

The tool, that allows you to create those mentioned files, is called `mk_patt`. It is part of the ARToolkit. You can download the windows binaries from <a href='http://sourceforge.net/projects/artoolkit/files/artoolkit/2.72.1/ARToolKit-2.72.1-bin-win32.zip/download'>here</a>. The source code (e.g. for linux systems) can be downloaded <a href='http://sourceforge.net/projects/artoolkit/files/artoolkit/2.72.1/ARToolKit-2.72.1.tgz/download'>here</a>.

  1. <a href='http://sourceforge.net/projects/artoolkit/files/artoolkit/2.72.1/ARToolKit-2.72.1-bin-win32.zip/download'>Download</a> the mentioned package.
  1. If your not using windows you have to compile the binaries from the source code.
  1. The executable can be found in the `bin` folder.
  1. Download the glut32.dll from <a href='http://www.xmission.com/~nate/glut.html'>here</a> and place it into the `bin` folder. (windows only)
  1. Execute `mk_patt`. A command line window will appear. Hit the enter key.
  1. A live video stream from your webcam should appear.
  1. Hold your marker into the camera. The camera should be rotated until the red corner of the highlighted square is the top left hand corner of the square in the video image.
  1. Now click into the window.
  1. Enter a file name in the command line window.
  1. Copy this file into the `assets` folder of the Eclipse project.
  1. Refer to this file through the ARObject's constructor. (ARObject is abstract, so have a look at the class extending it, like the Model class in the AndAR Model Viewer)
  1. That's it!