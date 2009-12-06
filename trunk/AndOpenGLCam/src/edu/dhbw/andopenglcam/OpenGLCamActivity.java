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

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;

public class OpenGLCamActivity extends Activity {
	private GLSurfaceView glSurfaceView;
	private Camera camera;
	private OpenGLCamRenderer renderer;
	private Resources res;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        camera = Camera.open();        
        //get the pixel format of the camera
        //the default is YCbCr_420_SP (NV21), see:
        int pixelFormat = camera.getParameters().getPreviewFormat();
        glSurfaceView = new GLSurfaceView(this);
        try {
			renderer = new OpenGLCamRenderer(pixelFormat, res);
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
        glSurfaceView.setRenderer(renderer);
        setContentView(glSurfaceView);        
        camera.setPreviewCallback(renderer);
        camera.startPreview();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        //release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        if (camera == null) {
	        //reobtain camera
	        camera = Camera.open();
	        camera.setPreviewCallback(renderer);
	        camera.startPreview();
        }
    }

}