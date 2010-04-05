/**
 * 
 */
package edu.dhbw.andar;


import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.interfaces.PreviewFrameSink;

import android.util.Log;

/**
 * stores all information about the markers
 * @author Tobias Domhan
 *
 */
public class MarkerInfo {
	private int markerNum = -1;
	//private double[] glTransMat = new double[16];
	private boolean initialized = false;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private int imageWidth = 0;
	private int imageHeight = 0;
	/**
	 * The transformation matrix is accessed, when drawing the object(read),
	 * but is also written to when detecting the markers from a different thread.
	 */
	private Object transMatMonitor = new Object();
	private DetectMarkerWorker detectMarkerWorker = new DetectMarkerWorker();
	
	/**
	 * native libraries
	 */
	static {
		//arToolkit
		System.loadLibrary( "ar" );
	}
	
	/**
	 * initialize the artoolkit
	 * @param imageWidth width of the image data
	 * @param imageHeight height of the image data
	 * @param screenWidth width of the screen
	 * @param screenHeight height of the screen
	 */
	private native void artoolkit_init(int imageWidth, int imageHeight,
			int screenWidth, int screenHeight);
	
	/**
	 * detect the markers in the frame
	 * @param in the image 
	 * @param matrix the transformation matrix for each marker
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
		Log.i("MarkerInfo", "setting image width("+width+") and height("+height+")");
		this.imageWidth = width;
		this.imageHeight = height;
		initialize();
	}
	
	private void initialize() {
		//make sure all sizes are set
		if(screenWidth>0 && screenHeight>0&&imageWidth>0&&imageHeight>0) {
			Log.i("MarkerInfo", "going to initialize the native library now");
			artoolkit_init(imageWidth, imageHeight, screenWidth, screenHeight);
			Log.i("MarkerInfo", "alright, done initializing the native library");
			initialized = true;
		}
	}
	
	/**
	 * Detects the markers in the image, and updates the state of the
	 * MarkerInfo accordingly.
	 * @param image
	 */
	public void detectMarkers(byte[] image) {
		//make sure we initialized the native library
		if(initialized) {			
			detectMarkerWorker.nextFrame(image);
		}
	}
	
	public void draw(GL10 gl) {
		if(initialized) {
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
		
		synchronized void nextFrame(byte[] frame) {
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
