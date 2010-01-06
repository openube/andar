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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.dhbw.andopenglcam.interfaces.PreviewFrameSink;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.opengl.GLSurfaceView;

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
	
	/**
	 * 
	 */
	public CameraPreviewHandler(GLSurfaceView glSurfaceView, PreviewFrameSink sink) {
		this.glSurfaceView = glSurfaceView;
		this.frameSink = sink;
	}

	//size of a texture must be a power of 2
	private byte[] frame=new byte[256*256];
	
	/* 
	 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		//prevent null pointer exceptions
		if (data == null) return;
		//TODO set width/height dynamically
		//width 480
		//height 320
		//...
		frameSink.getFrameLock().lock();
   		int bwCounter=0;
   		int yuvsCounter=0;
   		for (int y=0;y<160;y++) {
   			System.arraycopy(data, yuvsCounter, frame, bwCounter, 240);
   			yuvsCounter=yuvsCounter+240;
   			bwCounter=bwCounter+256;
   		}
   		
		frameSink.setNextFrame(ByteBuffer.wrap(frame));
		this.glSurfaceView.requestRender();
		frameSink.getFrameLock().unlock();
	}

}
