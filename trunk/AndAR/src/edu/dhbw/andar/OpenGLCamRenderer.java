/**
	Copyright (C) 2009  Tobias Domhan

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.interfaces.PreviewFrameSink;



import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera.Size;
import android.opengl.GLDebugHelper;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

/**
 * Opens the camera and displays the output on a square (as a texture)
 * @author Tobias Domhan
 *
 */
public class OpenGLCamRenderer implements Renderer, PreviewFrameSink{
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
	private float[] specref = {0.99f,	0.94f,	0.81f,	1.0f};
	private FloatBuffer specrefBuffer = makeFloatBuffer(specref);
	private float[] difcref = {0.78f,	0.57f,	0.11f,	1.0f};
	private FloatBuffer difcrefBuffer = makeFloatBuffer(difcref);
	private float[] ambref = {	0.33f,	0.22f,	0.03f,	1.0f};
	private FloatBuffer ambrefBuffer = makeFloatBuffer(ambref);
	
	private FloatBuffer textureBuffer;
	private FloatBuffer squareBuffer;
	private boolean frameEnqueued = false;
	private ByteBuffer frameData = null;
	private ReentrantLock frameLock = new ReentrantLock();
	private boolean isTextureInitialized = false;
	private Writer log = new LogWriter();
	private int textureSize = 256;
	private int previewFrameWidth = 256;
	private int previewFrameHeight = 256;
	private MarkerInfo markerInfo;
	private float aspectRatio=1;
	
	/**
	 * mode, being either GL10.GL_RGB or GL10.GL_LUMINANCE
	 */
	private int mode = GL10.GL_RGB;
	
	/**
	 * the default constructer
	 * @param int the {@link PixelFormat} of the Camera preview
	 * @param res Resources
	 */
	public OpenGLCamRenderer(Resources res, MarkerInfo markerInfo)  {
		this.res = res;
		this.markerInfo = markerInfo;
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		if(DEBUG)
			gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		setupDraw2D(gl);
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

		markerInfo.draw(gl);
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
		gl.glEnable(GL10.GL_CULL_FACE);
		
		int[] textureNames = new int[1];
		//generate texture names:
		gl.glGenTextures(1, textureNames, 0);
		textureName = textureNames[0];
		
		textureBuffer = makeFloatBuffer(textureCoords);
		
		//lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, makeFloatBuffer(ambientlight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, makeFloatBuffer(diffuselight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, makeFloatBuffer(specularlight));
		gl.glEnable(GL10.GL_LIGHT0);
		
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		
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
	public void setNextFrame(ByteBuffer buf) {
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
