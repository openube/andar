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


import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.interfaces.PreviewFrameSink;



import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.Config;
import android.opengl.GLDebugHelper;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

/**
 * Opens the camera and displays the output on a square (as a texture)
 * @author Tobias Domhan
 *
 */
public class AndARRenderer implements Renderer, PreviewFrameSink{
	private Resources res;
	private boolean DEBUG = false;
	private int textureName;
	private float[] square;
	float[] textureCoords = new float[] {
			// Camera preview
			 0.0f, 0.625f,
			 0.9375f, 0.625f,
			 0.0f, 0.0f,
			 0.9375f, 0.0f			 
		};
	
	/**
	 * Light definitions
	 */
	private float[] ambientlight = {.3f, .3f, .3f, 1f};
	private float[] diffuselight = {.7f, .7f, .7f, 1f};
	private float[] specularlight = {0.6f, 0.6f, 0.6f, 1f};
	private float[] lightposition = {100.0f,-200.0f,200.0f,0.0f};
	
	private FloatBuffer lightPositionBuffer =  makeFloatBuffer(lightposition);
	private FloatBuffer specularLightBuffer = makeFloatBuffer(specularlight);
	private FloatBuffer diffuseLightBuffer = makeFloatBuffer(diffuselight);
	private FloatBuffer ambientLightBuffer = makeFloatBuffer(ambientlight);
	
	private FloatBuffer textureBuffer;
	private FloatBuffer squareBuffer;
	private boolean frameEnqueued = false;
	private boolean takeScreenshot = false;
	private Bitmap screenshot;
	private Object screenshotMonitor = new Object();
	private boolean screenshotTaken = false;
	private ByteBuffer frameData = null;
	private ReentrantLock frameLock = new ReentrantLock();
	private boolean isTextureInitialized = false;
	private Writer log = new LogWriter();
	private int textureSize = 256;
	private int previewFrameWidth = 256;
	private int previewFrameHeight = 256;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private ARToolkit markerInfo;
	private float aspectRatio=1;
	private OpenGLRenderer customRenderer;
	private AndARActivity activity;
	
	/**
	 * mode, being either GL10.GL_RGB or GL10.GL_LUMINANCE
	 */
	private int mode = GL10.GL_RGB;
	
	/**
	 * the default constructer
	 * @param int the {@link PixelFormat} of the Camera preview
	 * @param res Resources
	 * @param customRenderer non AR renderer, may be null
	 */
	public AndARRenderer(Resources res, ARToolkit markerInfo, AndARActivity activity)  {
		this.res = res;
		this.markerInfo = markerInfo;
		this.activity = activity;
	}
	

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public final void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);		
		
		if(DEBUG)
			gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		setupDraw2D(gl);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
		//load new preview frame as a texture, if needed
		if (frameEnqueued) {
			frameLock.lock();
			if(!isTextureInitialized) {
				initializeTexture(gl);
			} else {
				//just update the image
				//can we just update a portion(non power of two)?...seems to work
				gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, previewFrameWidth, previewFrameHeight,
						mode, GL10.GL_UNSIGNED_BYTE, frameData);
			}
			frameLock.unlock();
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			frameEnqueued = false;
		}
		
		gl.glColor4f(1, 1, 1, 1f);	
		//draw camera preview frame:
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareBuffer);
		
		//draw camera square
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		if(customRenderer != null)
			customRenderer.setupEnv(gl);
		else {
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLightBuffer);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLightBuffer);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularLightBuffer);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
			gl.glEnable(GL10.GL_LIGHT0);
		}
		
		markerInfo.draw(gl);
		
		if(customRenderer != null)
			customRenderer.draw(gl);
		
		//take a screenshot, if desired
		if(takeScreenshot) {
			//http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html
			takeScreenshot = false;
			int[] tmp = new int[screenHeight*screenWidth];
			int[] screenshot = new int[screenHeight*screenWidth];
			Buffer screenshotBuffer = IntBuffer.wrap(tmp);
			screenshotBuffer.position(0);
			gl.glReadPixels(0,0,screenWidth,screenHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, screenshotBuffer); 
			for(int i=0; i<screenHeight; i++) 
	         {//remember, that OpenGL bitmap is incompatible with Android bitmap 
	          //and so, some correction need.      
	              for(int j=0; j<screenWidth; j++) 
	              { 
	                   int pix=tmp[i*screenWidth+j]; 
	                   int pb=(pix>>16)&0xff; 
	                   int pr=(pix<<16)&0x00ff0000; 
	                   int pix1=(pix&0xff00ff00) | pr | pb; 
	                   screenshot[(screenHeight-i-1)*screenWidth+j]=pix1; 
	              } 
	         }  
			this.screenshot = Bitmap.createBitmap(screenshot, screenWidth, screenHeight, Config.RGB_565);
			
			screenshotTaken = true;
			//wake up the waiting method
			synchronized (screenshotMonitor) {
				screenshotMonitor.notifyAll();
			}			
		}
	}
	

	/* 
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		//TODO handle landscape view
		gl.glViewport(0, 0, width, height);
		aspectRatio = (float)width/(float)height;
		setupDraw2D(gl);		
		square = new float[] { 	-100f*aspectRatio, -100.0f, -1f,
				 100f*aspectRatio, -100.0f, -1f,
				-100f*aspectRatio, 100.0f, -1f,
				 100f*aspectRatio, 100.0f, -1f };
		
		squareBuffer = makeFloatBuffer(square);		
		markerInfo.setScreenSize(width, height);
		screenHeight = height;
		screenWidth = width;
	}
	
	/**
	 * Setup OpenGL to draw in 2D.
	 */
	private void setupDraw2D(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(-100.0f*aspectRatio, 100.0f*aspectRatio, -100.0f, 100.0f, 1.0f, -1.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0,0,0,0);
		gl.glClearDepthf(1.0f);
		//enable textures:
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		int[] textureNames = new int[1];
		//generate texture names:
		gl.glGenTextures(1, textureNames, 0);
		textureName = textureNames[0];
		
		textureBuffer = makeFloatBuffer(textureCoords);

		
		//register unchaught exception handler
		Thread.currentThread().setUncaughtExceptionHandler(activity);
		
		markerInfo.initGL(gl);
		if(customRenderer != null)
			customRenderer.initGL(gl);
	}
	
	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	protected static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}


	/* (non-Javadoc)
	 * @see edu.dhbw.andopenglcam.interfaces.PreviewFrameSink#setNextFrame(java.nio.ByteBuffer)
	 */
	@Override
	public final void setNextFrame(ByteBuffer buf) {
		this.frameData = buf;
		this.frameEnqueued = true;
	}


	/* (non-Javadoc)
	 * @see edu.dhbw.andopenglcam.interfaces.PreviewFrameSink#getFrameLock()
	 */
	@Override
	public ReentrantLock getFrameLock() {
		return frameLock;
	}

	/* Set the size of the texture(must be power of two)
	 * @see edu.dhbw.andopenglcam.interfaces.PreviewFrameSink#setTextureSize()
	 */
	@Override
	public void setPreviewFrameSize(int textureSize, int realWidth, int realHeight) {
		//test if it is a power of two number
		if (!GenericFunctions.isPowerOfTwo(textureSize))
			return;
		this.textureSize = textureSize;
		this.previewFrameHeight = realHeight;
		this.previewFrameWidth = realWidth;
		//calculate texture coords
		this.textureCoords = new float[] {
				// Camera preview
				 0.0f, ((float)realHeight)/textureSize,
				 ((float)realWidth)/textureSize, ((float)realHeight)/textureSize,
				 0.0f, 0.0f,
				 ((float)realWidth)/textureSize, 0.0f			 
			};		
		textureBuffer= makeFloatBuffer(textureCoords);		
	}
	
	private void initializeTexture (GL10 gl) {
		byte[] frame;
		switch(mode) {
		default:
			mode = GL10.GL_RGB;
		case GL10.GL_RGB:
			frame = new byte[textureSize*textureSize*3];
			break;
		case GL10.GL_LUMINANCE:
			frame = new byte[textureSize*textureSize];
			break;
		}
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, mode, textureSize,
				textureSize, 0, mode, GL10.GL_UNSIGNED_BYTE ,
				ByteBuffer.wrap(frame));
		isTextureInitialized = true;		
	}
	
	/**
	 * sets the mode(either GL10.GL_RGB or GL10.GL_LUMINANCE)
	 * @param pMode
	 */
	public void setMode(int pMode) {
		switch(pMode) {		
		case GL10.GL_RGB:
		case GL10.GL_LUMINANCE:
			this.mode = pMode;
			break;
		default:
			this.mode = GL10.GL_RGB;
			break;
		}
		if(pMode != this.mode)
			isTextureInitialized = false;
	}

	public void setNonARRenderer(OpenGLRenderer customRenderer) {
		this.customRenderer = customRenderer;
	}

	public Bitmap takeScreenshot() {
		synchronized (screenshotMonitor) {
			screenshotTaken = false;
			takeScreenshot = true;
			while(!screenshotTaken) {
				//protect against spurios wakeups
				try {
					screenshotMonitor.wait();
				} catch (InterruptedException e) {}
			}
		}		
		return screenshot;
	}
	
	
	
}

/**
 * write stuff to Android log
 * @author Tobias Domhan
 *
 */
class LogWriter extends Writer {

    @Override public void close() {
        flushBuilder();
    }

    @Override public void flush() {
        flushBuilder();
    }

    @Override public void write(char[] buf, int offset, int count) {
        for(int i = 0; i < count; i++) {
            char c = buf[offset + i];
            if ( c == '\n') {
                flushBuilder();
            }
            else {
                mBuilder.append(c);
            }
        }
    }

    private void flushBuilder() {
        if (mBuilder.length() > 0) {
            Log.e("OpenGLCam", mBuilder.toString());
            mBuilder.delete(0, mBuilder.length());
        }
    }

    private StringBuilder mBuilder = new StringBuilder();
    
    
}
