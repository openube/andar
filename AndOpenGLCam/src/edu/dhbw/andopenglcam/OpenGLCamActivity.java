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
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
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
        setFullscreen();
        disableScreenTurnOff();
        //orientation is set via the manifest

        res = getResources();     
        glSurfaceView = new OpenGLCamView(this);
		renderer = new OpenGLCamRenderer(res);
		cameraHandler = new CameraPreviewHandler(glSurfaceView, renderer, res);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.getHolder().addCallback(this);
        setContentView(glSurfaceView); 	 
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
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    private void openCamera()  {
    	if (camera == null) {
	    	//camera = Camera.open();
    		camera = CameraHolder.instance().open();
	        Parameters params = camera.getParameters();
	        params.setPreviewSize(240,160);
	        //params.setPreviewFrameRate(10);//TODO remove restriction
	        camera.setParameters(params);
	        camera.setPreviewCallback(cameraHandler);
	        //camera.setOneShotPreviewCallback(cameraHandler);
	        try {
				cameraHandler.init(camera);
			} catch (Exception e) {
				//TODO: notify the user
			}
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

	/* The GLSurfaceView changed
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/* The GLSurfaceView was created
	 * The camera will be opened and the preview started 
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(!mPreviewing)
			startPreview();  
	}

	/* GLSurfaceView was destroyed
	 * The camera will be closed and the preview stopped.
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        closeCamera();
	}

}