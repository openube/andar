/**
 * 
 */
package edu.dhbw.andar;


import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.exceptions.AndARException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Interface to the ARToolkit.
 * @author Tobias Domhan
 *
 */
public class ARToolkit {
	private final String calibFileName = "camera_para.dat";
	private int markerNum = -1;
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
	private Vector<ARObject> arobjects = new Vector<ARObject>();
	/**
	 * absolute path of the local files:
	 * the calib file will be stored there, among other things
	 */
	private String baseFolder;
	
	
	public ARToolkit(String baseFile) {
		artoolkit_init();
		this.baseFolder = baseFile;
	}
	
	/**
	 * Registers an object to the ARToolkit. This means:
	 * The toolkit will try to determine the pose of the object.
	 * If it is visible the draw method of the object will be invoked.
	 * The corresponding translation matrix will be applied inside opengl
	 * before doing so.
	 * @param arobject The object that shell be registered.
	 */
	public synchronized void registerARObject(ARObject arobject) {
		arobjects.add(arobject);
		arobject.setId(nextObjectID);
		
		nextObjectID++;
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
	 * Register a object to the native library.
	 * @param id a unique ID of the object
	 * @param patternName the fileName of the pattern
	 * @param markerWidth the width of the object
	 * @param markerCenter the center of the object
	 */
	private native void addObject(int id, String patternName, double markerWidth, double[] markerCenter);
	
	/**
	 * Remove the object from the list of registered objects.
	 * @param id
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
	
	private native void draw();
	
	/**
	 * 
	 * @param width of the screen
	 * @param height of the screen
	 */
	public void setScreenSize(int width, int height) {
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
	public void setImageSize(int width, int height) {
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
			artoolkit_init(baseFolder+"a"+File.separator+calibFileName, imageWidth, imageHeight, screenWidth, screenHeight);	
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
	public void draw(GL10 gl) {
		if(initialized) {
			if(Config.DEBUG)
				Log.i("MarkerInfo", "going to draw opengl stuff now");
			synchronized (transMatMonitor) {
				draw();
			}
		}
	}
	
	class DetectMarkerWorker extends Thread {
		private byte[] curFrame;
		
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
			try {
				wait();//wait for initial frame
			} catch (InterruptedException e) {}
			while(true) {
				//the monitor is locked inside the method
				artoolkit_detectmarkers(curFrame, transMatMonitor);
				try {
					wait();//wait for next frame
				} catch (InterruptedException e) {}
			}
		}
		
		synchronized final void nextFrame(byte[] frame) {
			if(this.getState() == Thread.State.WAITING) {
				//ok, we are ready for a new frame:
				curFrame = frame;
				//do the work:
				this.notify();
			} else {
				//ignore it
			}
		}
	}
	
	

}
