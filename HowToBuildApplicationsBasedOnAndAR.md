# License #

The whole project is released under the **GNU General Public License**. This means it can be used in any project that is itself released under the GPL. If you would like to create a commercial application based on AndAR please contact [ARToolworks](http://www.artoolworks.com/Home.html).


# Eclipse sample project Howto #
This section is about the Eclipse sample project, alternatively you may check out the source code from the SVN <a href='http://code.google.com/p/andar/source/checkout'>repository</a>.

  * [Download](http://www.eclipse.org/downloads/) and install [Eclipse](http://www.eclipse.org/).(Eclipse IDE for Java Developers)
  * Install the Android Plugin for Eclipse, as described in the [Android docs](http://developer.android.com/guide/developing/eclipse-adt.html).
  * Download the Eclipse sample project, [here](http://andar.googlecode.com/files/AndARSampleProject.zip).
  * Unzip it somewhere
  * Now start Eclipse.
  * File -> Import
  * General -> Existing projects into workspace
  * Select the unzipped folder
  * enjoy :D
  * Send me an [email](mailto:tdomhan@gmail.com), so that I can add you to the [http://code.google.com/p/andar/wiki/ProjectsUsingAndAR](ProjectsUsingAndAR.md) wiki page.

# AndAR Architecture #
<img src='http://andar.googlecode.com/files/andararch.png' />

AndAR is an Augmented Reality Framework for Android. It not only offers a pure
Java API but is also object oriented. The figure above shows a simplied class diagram of
an application that makes use of AndAR.

Every Android application consists of one or more Activities. An Activity is a visual
user interface, targeted to a single purpose. Only one may be active at a time. In
order to write an Augmented Reality application, one has to extend the abstract class
AndARActivity. This class already handles everything Augmented Reality related,
like opening the camera, detecting the markers and displaying the video stream. The
application would run already, by just doing that. However it would not detect any
markers.

In order to do so, you have to register ARObjects to an instance of ARToolkit. This
instance can be retrieved from the AndARActivity. The ARObject class itself is abstract.
This means, it has to be extended, too. It expects the file name of a pattern file
in it's constructor. This file must be located in the `assets` folder of the Eclipse project.

Pattern files can be created by a tool called mk\_patt, as described <a href='http://code.google.com/p/andar/wiki/HowToUseYourOwnMarkers'>here</a>.
They are used to distinguish different markers. In order to draw a custom object, the
method draw has to be overridden. Before this method is invoked a transformation
matrix will already have been applied. This means the object will be alligned to the
marker, without any further steps. This method will not be invoked, if the marker
belonging to this object is not visible.

The class ARRenderer is reponsible for everything OpenGL related. If you want to mix
augmented with non augmented 3D objects you may provide a class implementing the
OpenGLRenderer interface. There are three methods defined by this interface. `initGL` being called only once, when the OpenGL surface is initialized. Whereas `setupEnv` is
called once before the augmented objects are drawn. It can be used to issue OpenGL
commands that shall effect all ARObjects, like initializing the lighting. In the draw
method you may draw any non augmented 3D objects. It will be called once for every
frame. Specifying such the described renderer is optional.
The AndARActivity furthermore offers a method that allows the application to take
screenshots.