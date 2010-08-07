/**
	Copyright (C) 2009,2010  Tobias Domhan

    This file is part of AndOpenGLCam.

    AndObjViewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AndObjViewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AndObjViewer.  If not, see <http://www.gnu.org/licenses/>.
 
 */
package edu.dhbw.andar;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.util.Log;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andar.interfaces.MarkerVisibilityListener;
import edu.dhbw.andar.util.GraphicsUtil;
import edu.dhbw.andar.util.IO;

/**
 * Interface to the ARToolkit.
 * @author Tobias Domhan
 *
 */
public class ARToolkit {
	private final Resources res;
	private final String calibFileName = "camera_para.dat";
	//private double[] glTransMat = new double[16];
	private boolean initialized = false;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private int imageWidth = 0;
	private int imageHeight = 0;
	/**
	 * Every object get'S his own unique ID. 
	 * This counter may never be decremented.
	 */
	private int nextObjectID = 0;
	/**
	 * The transformation matrix is accessed, when drawing the object(read),
	 * but is also written to when detecting the markers from a different thread.
	 */
	private Object transMatMonitor = new Object();
	private DetectMarkerWorker detectMarkerWorker = new DetectMarkerWorker();
	private List<MarkerVisibilityListener> visListeners = new ArrayList<MarkerVisibilityListener>();
	private Vector<ARObject> arobjects = new Vector<ARObject>();
	/**
	 * absolute path of the local files:
	 * the calib file will be stored there, among other things
	 */
	private File baseFolder;
	
	
	public ARToolkit(Resources res, File baseFile) {
		artoolkit_init();
		this.baseFolder = baseFile;
		this.res = res;
	}
	
	/**
	 * Registers an object to the ARToolkit. This means:
	 * The toolkit will try to determine the pose of the object.
	 * If it is visible the draw method of the object will be invoked.
	 * The corresponding translation matrix will be applied inside opengl
	 * before doing so.
	 * TODO: registering a object with the same pattern twice will not work, as arloadpatt will create different IDs for the same pattern, and the detecting function will return only the first id as being detected. we need to store patt load id's in an hash -> loadpatt as a native function returning the ID -> pass this id to the object registering function.
	 * @param arobject The object that shell be registered.
	 */
	public synchronized void registerARObject(ARObject arobject) 
		throws AndARException{	
		if(arobjects.contains(arobject)) 
			return;//don't register the same object twice
		try {
			//transfer pattern file to private space
			IO.transferFileToPrivateFS(baseFolder,
					arobject.getPatternName(), res);
			arobjects.add(arobject);
			arobject.setId(nextObjectID);
			String patternFile = baseFolder.getAbsolutePath() + File.separator + 
			arobject.getPatternName();
			addObject(nextObjectID, arobject, patternFile,
					arobject.getMarkerWidth(), arobject.getCenter());
			nextObjectID++;
		} catch (IOException e) {
			e.printStackTrace();
			throw new AndARException(e.getMessage());
		}		
	}
	
	
	public synchronized void unregisterARObject(ARObject arobject) {
		if(arobjects.contains(arobject)) {
			arobjects.remove(arobject);
			//remove from the native library
			removeObject(arobject.getId());
		}
	}
	
	/**
	 * native libraries
	 */
	static {
		//arToolkit
		System.loadLibrary( "ar" );
	}
	
	/**
	 * Register a object to the native library. From now on the detection function will determine
	 * if the given object is visible on a marker, and set the transformation matrix accordingly.
	 * @param id a unique ID of the object
	 * @param patternName the fileName of the pattern
	 * @param markerWidth the width of the object
	 * @param markerCenter the center of the object
	 */
	private native void addObject(int id, ARObject obj, String patternName, double markerWidth, double[] markerCenter);
	
	/**
	 * Remove the object from the list of registered objects.
	 * @param id the id of the object.
	 */
	private native void removeObject(int id);
	
	/**
	 * Do some basic initialization, like creating data structures.
	 */
	private native void artoolkit_init();
	
	/**
	 * Do initialization specific to the image/screen dimensions.
	 * @param imageWidth width of the image data
	 * @param imageHeight height of the image data
	 * @param screenWidth width of the screen
	 * @param screenHeight height of the screen
	 */
	private native void artoolkit_init(String filesFolder,int imageWidth, int imageHeight,
			int screenWidth, int screenHeight);
	
	/**
	 * detect the markers in the frame
	 * @param in the image 
	 * @param matrix the transformation matrix for each marker, will be locked right before the trans matrix will be altered
	 * @return number of markers
	 */
	private native int artoolkit_detectmarkers(byte[] in, Object transMatMonitor);
	
	/**
	 * Inverse a three by four matrix.
	 * Matrix has to be 3 by 4! (but is actually a 4 by 4 homogene matrix.)
	 * @param mat1 contains the matrix to be inversed.
	 * @param mat2 will contain the resulting matrix.
	 * @return success?
	 */
	public native static int  arUtilMatInv(double[] mat1, double[] mat2);
	
	/**
	 * Multiply one (three by four) matrix by another.
	 * Matrix has to be 3 by 4! (but is actually a 4 by 4 homogene matrix.)
	 * @param multiplier
	 * @param multiplicand
	 * @param result contains the result, after the method returns.
	 * @return
	 */
	public native static int  arUtilMatMul(double[] multiplier, double[] multiplicand, double[] result);
	
	
	/**
	 * 
	 * @param width of the screen
	 * @param height of the screen
	 */
	protected void setScreenSize(int width, int height) {
		if(Config.DEBUG)
			Log.i("MarkerInfo", "setting screen width("+width+") and height("+height+")");
		this.screenWidth = width;
		this.screenHeight = height;
		initialize();
	}
	
	/**
	 * 
	 * @param width of the image
	 * @param height of the image
	 */
	protected void setImageSize(int width, int height) {
		if(Config.DEBUG)
			Log.i("MarkerInfo", "setting image width("+width+") and height("+height+")");
		this.imageWidth = width;
		this.imageHeight = height;
		initialize();
	}
	
	private void initialize() {
		//make sure all sizes are set
		if(screenWidth>0 && screenHeight>0&&imageWidth>0&&imageHeight>0) {
			if(Config.DEBUG)
				Log.i("MarkerInfo", "going to initialize the native library now");
			artoolkit_init(baseFolder+File.separator+calibFileName, imageWidth, imageHeight, screenWidth, screenHeight);	
			ARObject.glCameraMatrixBuffer = GraphicsUtil.makeFloatBuffer(ARObject.glCameraMatrix);
			if(Config.DEBUG)
				Log.i("MarkerInfo", "alright, done initializing the native library");
			initialized = true;
		}
	}
	
	/**
	 * Detects the markers in the image, and updates the state of the
	 * MarkerInfo accordingly.
	 * @param image
	 */
	public final void detectMarkers(byte[] image) {
		//make sure we initialized the native library
		if(initialized) {			
			detectMarkerWorker.nextFrame(image);
		}
	}
	
	/**
	 * Draw all ARObjects.
	 * @param gl
	 */
	public final void draw(GL10 gl) {
		if(initialized) {
			if(Config.DEBUG)
				Log.i("MarkerInfo", "going to draw opengl stuff now");
			for (ARObject obj : arobjects) {
				if(obj.isVisible())
					obj.draw(gl);
			}
		}
	}
	
	/**
	 * initialize the objects.
	 * @param gl
	 */
	public final void initGL(GL10 gl) {
		for (ARObject obj : arobjects) {
			if(obj.isVisible())
				obj.init(gl);
		}
	}
	
	
	
	/** 
	 * @param visListener listener to add to the registered listeners.
	 * @deprecated Use addVisibilityListener instead.
	 */
	@Deprecated
	public void setVisListener(MarkerVisibilityListener visListener) {		
		this.visListeners.add(visListener);
	}
	
	public void addVisibilityListener(
			MarkerVisibilityListener markerVisibilityListener) {
		visListeners.add(markerVisibilityListener);		
	}



	class DetectMarkerWorker extends Thread {
		private byte[] curFrame;
		private boolean newFrame = false;
		private int lastNumMarkers=0;
		
		/**
		 * 
		 */
		public DetectMarkerWorker() {
			setPriority(MIN_PRIORITY);
			setDaemon(true);
			start();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public synchronized void run() {			
			setName("DetectMarkerWorker");
			while(true) {
				while(!newFrame) {
					//spurious wakeups
					try {
						wait();//wait for next frame
					} catch (InterruptedException e) {}
				}
				newFrame = false;
				//the monitor is locked inside the method
				int currNumMakers = artoolkit_detectmarkers(curFrame, transMatMonitor);
				if(lastNumMarkers > 0 && currNumMakers > 0) {
					//visible
				} else if(lastNumMarkers == 0 && currNumMakers > 0) {
					//detected a marker
					notifyChange(true);
				} else if(lastNumMarkers > 0 && currNumMakers == 0) {
					//lost the marker
					notifyChange(false);
				}
				lastNumMarkers = currNumMakers;
			}
		}
		
		private void notifyChange(boolean visible) {
			for (final MarkerVisibilityListener visListener : visListeners) {
				visListener.makerVisibilityChanged(visible);
			}
		}
		
		final void nextFrame(byte[] frame) {
			if(this.getState() == Thread.State.WAITING) {
				//ok, we are ready for a new frame:
				curFrame = frame;
				newFrame = true;
				//do the work:
				synchronized (this) {
					this.notify();
				}	
			} else {
				//ignore it
			}
		}
	}
	
	

}
