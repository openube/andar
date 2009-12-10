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
package edu.dhbw.andopenglcam;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andopenglcam.interfaces.PreviewFrameSink;



import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLDebugHelper;
import android.opengl.GLU;
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
	private int textureName;
	private float[] square;
	float textureCoords[] = new float[] {
			// Camera preview
			 0.0f, 0.625f,
			 0.9375f, 0.625f,
			 0.0f, 0.0f,
			 0.9375f, 0.0f			 
		};
		
		/*new float[] {0.0f, 0.0f,
										 1.0f, 0.0f,
										 0.0f, 1.0f,
										 1.0f, 1.0f};*/
	private FloatBuffer textureBuffer;
	private FloatBuffer squareBuffer;
	private Size previewSize;
	private boolean frameEnqueued = false;
	private ByteBuffer frameData = null;
	private ReentrantLock frameLock = new ReentrantLock();
	private boolean isTextureInitialized = false;
	private Writer log = new LogWriter();
	
	/**
	 * the default constructer
	 * @param int the {@link PixelFormat} of the Camera preview
	 * @param res Resources
	 */
	public OpenGLCamRenderer(int pixelFormat, Resources res) throws Exception {		
		if ((pixelFormat != PixelFormat.YCbCr_420_SP) && false) {//TODO abfangen
			//unkown Pixel format
			throw new Exception(res.getString(R.string.error_unkown_pixel_format));
		}
		this.res = res;
		
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
		//load new preview frame as a texture, if needed
		if (frameEnqueued) {
			frameLock.lock();
			//isTextureInitialized = false;
			if(!isTextureInitialized) {
				gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, 256, 256,
						0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE, frameData);
				isTextureInitialized = true;
			} else {
				//just update the image
				gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, 256, 256, GL10.GL_LUMINANCE,
						GL10.GL_UNSIGNED_BYTE, frameData);
			}
			frameLock.unlock();
			
			/*Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.nehe2);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);*/
			//gl.glTexSubImage2D(type, 0, 0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			frameEnqueued = false;
		}
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glColor4f(1, 1, 1, 0.5f);		
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}
	

	/* 
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		//TODO handle landscape view
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		//http://developer.android.com/reference/android/opengl/GLU.html#gluPerspective%28javax.microedition.khronos.opengles.GL10,%20float,%20float,%20float,%20float%29
		//GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
		float aspectRatio = (float)width/(float)height;
		//gl.glOrthof(-100.0f, 100.0f, -100.0f/aspectRatio, 100.0f/aspectRatio, 1.0f, -1.0f);
		
		gl.glOrthof(-100.0f*aspectRatio, 100.0f*aspectRatio, -100.0f, 100.0f, 1.0f, -1.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		
		/*square = new float[] { 	-100f, -100.0f/aspectRatio, -1f,
								 100f, -100.0f/aspectRatio, -1f,
								-100f, 100.0f/aspectRatio, -1f,
								 100f, 100.0f/aspectRatio, -1f };*/
		square = new float[] { 	-100f*aspectRatio, -100.0f, -1f,
				 100f*aspectRatio, -100.0f, -1f,
				-100f*aspectRatio, 100.0f, -1f,
				 100f*aspectRatio, 100.0f, -1f };
		
		squareBuffer = makeFloatBuffer(square);		
		//bind vertex pointers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareBuffer);
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
		
		//bind texture pointers:
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
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
	
}


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
