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

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

public class OpenGLCamActivity extends Activity implements Callback{
	private GLSurfaceView glSurfaceView;
	private Camera camera;
	private OpenGLCamRenderer renderer;
	private Resources res;
	private CameraPreviewHandler cameraHandler;
	private boolean mPreviewing = false;
	private boolean mPausing = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no title:
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setOrientation();
        setFullscreen();
        disableScreenTurnOff();

        
        res = getResources();     
        //get the pixel format of the camera
        //the default is YCbCr_420_SP (NV21), see:
        //camera = Camera.open();
        //Parameters params = camera.getParameters();
        //int pixelFormat = params.getPreviewFormat();
        glSurfaceView = new OpenGLCamView(this);
        try {
			renderer = new OpenGLCamRenderer(PixelFormat.YCbCr_420_SP, res);
			cameraHandler = new CameraPreviewHandler(glSurfaceView, renderer);
	        glSurfaceView.setRenderer(renderer);
	        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	        glSurfaceView.getHolder().addCallback(this);
	        setContentView(glSurfaceView); 	        
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
    }
    
    public void disableScreenTurnOff() {
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
    			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    public void setOrientation()  {
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
    public void setFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
   
    public void setNoTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    } 
    
    @Override
    protected void onPause() {
    	mPausing = true;
        this.glSurfaceView.onPause();
        super.onPause();
    }
    
    

    @Override
    protected void onResume() {
    	mPausing = false;
    	glSurfaceView.onResume();
        super.onResume();
        //if(!mPreviewing)
        //	startPreview();  
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
    	super.onStop();
    	/*if (camera != null) {
	        //release camera
	        camera.stopPreview();
	        camera.release();
	        camera = null;
        }*/
    }
    
    private void openCamera()  {
    	if (camera == null) {
	    	//camera = Camera.open();
    		camera = CameraHolder.instance().open();
	        Parameters params = camera.getParameters();
	        //TODO don't make assumptions about preview size
	        params.setPreviewSize(240,160);
	        camera.setParameters(params);
	        camera.setPreviewCallback(cameraHandler);	        
    	}
    }
    
    private void closeCamera() {
        if (camera != null) {
        	CameraHolder.instance().keep();
        	CameraHolder.instance().release();
        	camera = null;
            mPreviewing = false;
        }
    }
    
    private void startPreview() {
    	if(mPausing) return;
    	if (mPreviewing) stopPreview();
    	openCamera();
    	camera.startPreview();
    	mPreviewing = true;
    }
    
    private void stopPreview() {
    	if (camera != null && mPreviewing) {
            camera.stopPreview();
        }
        mPreviewing = false;
    }

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(!mPreviewing)
			startPreview();  
		/*try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			closeCamera();
		}*/
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        closeCamera();
	}

}