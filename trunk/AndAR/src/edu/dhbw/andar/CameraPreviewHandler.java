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

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.util.Log;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andar.exceptions.AndARRuntimeException;
import edu.dhbw.andar.interfaces.MarkerVisibilityListener;
import edu.dhbw.andar.interfaces.PreviewFrameSink;
import edu.dhbw.andar.util.GraphicsUtil;
import edu.dhbw.andopenglcam.R;

/**
 * Handles callbacks of the camera preview
 * camera preview demo:
 * http://developer.android.com/guide/samples/ApiDemos/src/com/example/android/apis/graphics/CameraPreview.html
 * YCbCr 420 colorspace infos:
	 * http://wiki.multimedia.cx/index.php?title=YCbCr_4:2:0
	 * http://de.wikipedia.org/wiki/YCbCr-Farbmodell
	 * http://www.elektroniknet.de/home/bauelemente/embedded-video/grundlagen-der-videotechnik-ii-farbraum-gammakorrektur-digitale-video-signale/4/
 * @see android.hardware.Camera.PreviewCallback
 * @author Tobias Domhan
 *
 */
public class CameraPreviewHandler implements PreviewCallback {
	private GLSurfaceView glSurfaceView;
	private PreviewFrameSink frameSink;
	private ByteBuffer frameBuffer;
	private CameraConstFPS constFPS = null;	
	private AutoFocusHandler focusHandler = null;
	private Resources res;
	private int textureSize=256;
	private int previewFrameWidth=240;
	private int previewFrameHeight=160;
	private int bwSize=previewFrameWidth*previewFrameHeight;//size of the black/white image
	
	//Modes:
	public final static int MODE_RGB=0;
	public final static int MODE_GRAY=1;
	private int mode = MODE_GRAY;
	private Object modeLock = new Object();
	private ARToolkit markerInfo;
	private ConversionWorker convWorker;
	private Camera cam;
	private CameraStatus camStatus;
	private boolean threadsRunning = true;
	
	
	
	public CameraPreviewHandler(GLSurfaceView glSurfaceView,
			PreviewFrameSink sink, Resources res, ARToolkit markerInfo, CameraStatus camStatus) {
		this.glSurfaceView = glSurfaceView;
		this.frameSink = sink;
		this.res = res;
		this.markerInfo = markerInfo;
		convWorker = new ConversionWorker(sink);
		this.camStatus = camStatus;
	}
	
	/**
	 * native libraries
	 */
	static { 
	    //System.loadLibrary( "imageprocessing" );
	    System.loadLibrary( "yuv420sp2rgb" );	
	} 
	
	/**
	 * native function, that converts a byte array from ycbcr420 to RGB
	 * @param in
	 * @param width
	 * @param height
	 * @param textureSize
	 * @param out
	 */
	private native void yuv420sp2rgb(byte[] in, int width, int height, int textureSize, byte[] out);


	/**
	 * Returns the best pixel format of the list or -1 if none suites.
	 * @param listOfFormats
	 * @return
	 */
	public static int getBestSupportedFormat(List<Integer> listOfFormats) {
		int format = -1;
		for (Iterator<Integer> iterator = listOfFormats.iterator(); iterator.hasNext();) {
			Integer integer =  iterator.next();
			if(integer.intValue() == PixelFormat.YCbCr_420_SP) {
				//alright the optimal format is supported..let's return
				format = PixelFormat.YCbCr_420_SP;
				return format;
			} else if(integer.intValue() == PixelFormat.YCbCr_422_SP) {
				format = PixelFormat.YCbCr_422_SP;
				//this format is not optimal. do not return, a better format might be in the list.
			}
		}
		return format;
	}
	
	/**
	 * the size of the camera preview frame is dynamic
	 * we will calculate the next power of two texture size
	 * in which the preview frame will fit
	 * and set the corresponding size in the renderer
	 * how to decode camera YUV to RGB for opengl:
	 * http://groups.google.de/group/android-developers/browse_thread/thread/c85e829ab209ceea/d3b29d3ddc8abf9b?lnk=gst&q=YUV+420#d3b29d3ddc8abf9b
	 * @param camera
	 */
	protected void init(Camera camera)  {
		Parameters camParams = camera.getParameters();
		//check if the pixel format is supported
		if (camParams.getPreviewFormat() == PixelFormat.YCbCr_420_SP)  {
			setMode(MODE_RGB);
		} else if (camParams.getPreviewFormat() == PixelFormat.YCbCr_422_SP) {
			setMode(MODE_GRAY);
		} else {
			//Das Format ist semi planar, Erklärung:
			//semi-planar YCbCr 4:2:2 : two arrays, one with all Ys, one with Cb and Cr. 
			//Quelle: http://www.celinuxforum.org/CelfPubWiki/AudioVideoGraphicsSpec_R2
			throw new AndARRuntimeException(res.getString(R.string.error_unkown_pixel_format));
		}	
		//get width/height of the camera
		Size previewSize = camParams.getPreviewSize();
		previewFrameWidth = previewSize.width;
		previewFrameHeight = previewSize.height;
		textureSize = GenericFunctions.nextPowerOfTwo(Math.max(previewFrameWidth, previewFrameHeight));
		//frame = new byte[textureSize*textureSize*3];
		bwSize = previewFrameWidth * previewFrameHeight;
		frame = new byte[bwSize*3];		
		
		frameBuffer = GraphicsUtil.makeByteBuffer(frame.length);
		frameSink.setPreviewFrameSize(textureSize, previewFrameWidth, previewFrameHeight);
		markerInfo.setImageSize(previewFrameWidth, previewFrameHeight);
		threadsRunning = true;
		if(Config.USE_ONE_SHOT_PREVIEW) {
			constFPS  = new CameraConstFPS(5, camera);
			constFPS.start();
		}	
		if(focusHandler == null) {
			focusHandler = new AutoFocusHandler(camera);
			focusHandler.start();
			markerInfo.setVisListener(focusHandler);
		}
	}

	//size of a texture must be a power of 2
	private byte[] frame;
	
	/**
	 * new frame from the camera arrived. convert and hand over
	 * to the renderer
	 * how to convert between YUV and RGB:http://en.wikipedia.org/wiki/YUV#Y.27UV444
	 * Conversion in C-Code(Android Project):
	 * http://www.netmite.com/android/mydroid/donut/development/tools/yuv420sp2rgb/yuv420sp2rgb.c
	 * http://code.google.com/p/android/issues/detail?id=823
	 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)
	 */
	@Override
	public synchronized void onPreviewFrame(byte[] data, Camera camera) {
			//prevent null pointer exceptions
			if (data == null)
				return;
			if(cam==null)
				cam = camera;
			//camera.setPreviewCallback(null);
			convWorker.nextFrame(data);
			markerInfo.detectMarkers(data);
	}
	
	
	protected void setMode(int pMode) {
		synchronized (modeLock) {			
			this.mode = pMode;
			switch(mode) {
			case MODE_RGB:
				frameSink.setMode(GL10.GL_RGB);
				break;
			case MODE_GRAY:
				frameSink.setMode(GL10.GL_LUMINANCE);
				break;
			}
		}		
	}
	
	/**
	 * A worker thread that does colorspace conversion in the background.
	 * Need so that we can throw frames away if we can't handle the throughput.
	 * Otherwise the more and more frames would be enqueued, if the conversion did take
	 * too long.
	 * @author Tobias Domhan
	 *
	 */
	class ConversionWorker extends Thread {
		private byte[] curFrame;
		private boolean newFrame = false;
		private PreviewFrameSink frameSink;
		
		/**
		 * 
		 */
		public ConversionWorker(PreviewFrameSink frameSink) {
			setDaemon(true);
			this.frameSink = frameSink;
			start();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public synchronized void run() {	
			setName("ConversionWorker");
			while(true) {
				while(!newFrame) {
					//protect against spurious wakeups
					try {
						wait();//wait for next frame
					} catch (InterruptedException e) {}
				}
				newFrame = false;
				if(Config.DEBUG)
					Log.d("ConversionWorker","starting conversion");
				synchronized (modeLock) {
					switch(mode) {
					case MODE_RGB:
						//color:
						yuv420sp2rgb(curFrame, previewFrameWidth, previewFrameHeight, textureSize, frame);   
						if(Config.DEBUG)
							Log.d("ConversionWorker","handing frame over to sink");
						frameSink.getFrameLock().lock();
						frameBuffer.position(0);
						frameBuffer.put(frame);
						frameBuffer.position(0);
						frameSink.setNextFrame(frameBuffer);
						frameSink.getFrameLock().unlock();
						if(Config.DEBUG)
							Log.d("ConversionWorker","done converting");
						break;
					case MODE_GRAY:
						//luminace: 
						//we will copy the array, assigning a new reference will cause multihreading issues
						//frame = curFrame;//WILL CAUSE PROBLEMS, WHEN SWITCHING BACK TO RGB	
						frameSink.getFrameLock().lock();
						frameBuffer.position(0);
						frameBuffer.put(curFrame,0,bwSize);
						frameBuffer.position(0);
						frameSink.setNextFrame(frameBuffer);
						frameSink.getFrameLock().unlock();
						break;
					}
				}
				if(Config.USE_ONE_SHOT_PREVIEW && CameraPreviewHandler.this.constFPS != null) {
					//we may get a new frame now.
					synchronized(CameraPreviewHandler.this.constFPS) {
						CameraPreviewHandler.this.constFPS.notify();
					}					
				}
				glSurfaceView.requestRender();
				yield();
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
	
	/**
	 * ensures a constant minimum frame rate.
	 * @author tobi
	 *
	 */
	class CameraConstFPS extends Thread {
		
		private long waitTime;
		private Camera cam;
		
		public CameraConstFPS(int fps, Camera cam) {
			waitTime = (long)(1.0/fps*1000);
			this.cam = cam;
		}
		
		@Override
		public synchronized void run() {
			super.run();
			setName("CameraConstFPS");
			while(threadsRunning) {
				try {
					wait(waitTime);
				} catch (InterruptedException e) {}
				if(camStatus.previewing) {
					try {
						cam.setOneShotPreviewCallback(CameraPreviewHandler.this);
					} catch(RuntimeException ex) {
						//
					}
				}
			}			
		}
	}

	class AutoFocusHandler extends Thread implements AutoFocusCallback, MarkerVisibilityListener {
		
		private Camera camera;
		private long lastScan;
		private static final int MIN_TIME = 1500;
		private static final int ENSURE_TIME = 10000;
		
		private boolean visible = false;
		
		public AutoFocusHandler(Camera camera) {
			this.camera = camera;
		}
		
		@Override
		public synchronized void run() {
			super.run();
			setName("Autofocus handler");
			//do an initial auto focus
			if(camStatus.previewing) {
				camera.autoFocus(this);
				lastScan = System.currentTimeMillis();
			}
			while(threadsRunning) {
				try {
					wait(ENSURE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long currTime = System.currentTimeMillis();
				//if at least ENSURE_TIME has passed since the last scan
				//and no marker is visible, do an scan
				if(!visible && (currTime-lastScan)>ENSURE_TIME) {
					if(camStatus.previewing)  {
						camera.autoFocus(this);
						lastScan = currTime;
					}
				}
				yield();
			}
		}

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			
		}

		@Override
		public void makerVisibilityChanged(boolean visible) {
			this.visible = visible;
			if(!visible) {
				long currTime = System.currentTimeMillis();
				if((currTime-lastScan)>MIN_TIME) {
					if(camStatus.previewing) {
						camera.autoFocus(this);
						lastScan = currTime;
					}
				}	
			}
		}
	}
	
	public void stopThreads() {
		threadsRunning = false;
		if(constFPS!= null)
			constFPS.interrupt();
		if(focusHandler!= null)
			focusHandler.interrupt();
	}

}
