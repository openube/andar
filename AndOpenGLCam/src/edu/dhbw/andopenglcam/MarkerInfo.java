/**
 * 
 */
package edu.dhbw.andopenglcam;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.opengles.GL10;

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
	private native int artoolkit_detectmarkers(byte[] in);
	
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
			transMatLock.lock();
			artoolkit_detectmarkers(image);
			transMatLock.unlock();
		}
	}
	
	public void draw(GL10 gl) {
		if(initialized) {
			Log.i("MarkerInfo", "going to draw opengl stuff now");
			draw();
		}
	}
    
	/**
	 * lock the {@link glTransMat}, in order to use it.
	 */
	private ReentrantLock transMatLock = new ReentrantLock();
	/**
	 * @return the markerNum
	 */
	public int getMarkerNum() {
		return markerNum;
	}
	/**
	 * @param markerNum the markerNum to set
	 */
	public void setMarkerNum(int markerNum) {
		this.markerNum = markerNum;
	}
	/**
	 * @return the glTransMat
	 */
	/*public double[] getGlTransMat() {
		return glTransMat;
	}*/
	/**
	 * @return the transMatLock
	 */
	public ReentrantLock getTransMatLock() {
		return transMatLock;
	}
}
