# Introduction #

AndAR Model Viewer is an Android application that is capable of displaying 3d models on Augmented Reality markers. **warning: this app is still in BETA state**
The models have to be in wavefront obj format.

This application is available in the android market:


# Details #

## First steps ##

  * Install the application, either search for "AndAR Model Viewer, or use the following barcode:
<img src='http://andar.googlecode.com/files/modelviewer_qrcode.png' />
  * You need to install the OI File Manager, however the app will automatically point you to this app in the market upon the first start.
  * Print out the [marker](http://andar.googlecode.com/files/Android.pdf), upon which the models will be projected. The marker will look like this:
<img src='http://andar.googlecode.com/files/androidmarker.png' width='100' />
  * Select one of the internal models.
  * Wait for the application to load the model. This might take a while, that's perfectly normal. Just be patient:
<img src='http://andar.googlecode.com/files/screenshot_loading.png' />
  * Finally the result should look like this:
<img src='http://andar.googlecode.com/files/AndARScreenshot.jpg' />
  * You may change the size of the model by swiping the screen vertically.

## Taking a screenshot ##

  * First press the menu key.
  * Next press the button "Take a screenshot".
  * The application will now process the image. It will notfiy you, when it's finished.
  * The screenshot you just took can be found in the root folder of your sd-card. It will be named something like AndARScreenshot1234.png

## Transforming the model ##

  * Press the menu key and select the desired transformation mode. You may either scale, rotate or translate the model.
  * Scale: Slide you finger up and down the touch screen. This will enlarge and shorten the model, respectively.
  * Rotate: Slide your finger horizontally and vertically, this will rotate your model correspondingly.
  * Translate: Slide your finger horizontally and vertically, this will translate your model correspondingly.

## Custom models ##

The application is capable of showing custom wavefront obj models. Most 3d modelling software out there can export this format(e.g. 3ds max, Blender). There are currently some restrictions to the models:
  * Every face must have normals specified
  * The object must be triangulated, this means exactly 3 vertices per face.
  * Basic materials and textures are supported.
E.g. when exporting a model from blender make sure you check Normals and Triangulate.