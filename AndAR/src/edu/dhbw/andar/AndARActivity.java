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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andar.exceptions.AndARRuntimeException;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.IO;
import edu.dhbw.andopenglcam.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

public abstract class AndARActivity extends Activity implements Callback, UncaughtExceptionHandler{
	private GLSurfaceView glSurfaceView;
	private Camera camera;
	private AndARRenderer renderer;
	private Resources res;
	private CameraPreviewHandler cameraHandler;
	private boolean mPreviewing = false;
	private boolean mPausing = false;
	private ARToolkit artoolkit;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(this);
        res = getResources();
        
        artoolkit = new ARToolkit(res, getFilesDir());
        setFullscreen();
        disableScreenTurnOff();
        //orientation is set via the manifest
        
        try {
			IO.transferFilesToPrivateFS(getFilesDir(),res);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AndARRuntimeException(e.getMessage());
		}
        glSurfaceView = new OpenGLCamView(this);
		renderer = new AndARRenderer(res, artoolkit, this);
		cameraHandler = new CameraPreviewHandler(glSurfaceView, renderer, res, artoolkit);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.getHolder().addCallback(this);
        setContentView(glSurfaceView);
        if(Config.DEBUG)
        	Debug.startMethodTracing("AndAR");
    }
    
    
    /**
     * Set a renderer that draws non AR stuff. Optional, may be set to null or omited.
     * @param customRenderer
     */
    public void setNonARRenderer(OpenGLRenderer customRenderer) {
		renderer.setNonARRenderer(customRenderer);
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
    protected void onDestroy() {
    	super.onDestroy();
    	if(Config.DEBUG)
    		Debug.stopMethodTracing();
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
	        
	        //try to set the preview format
	        params.setPreviewFormat(PixelFormat.YCbCr_420_SP);
	        camera.setParameters(params);
	        if (Integer.parseInt(Build.VERSION.SDK) <= 4) {
	        	//for android 1.5 compatibilty reasons:
				 /*try {
				    camera.setPreviewDisplay(glSurfaceView.getHolder());
				 } catch (IOException e1) {
				        e1.printStackTrace();
				 }*/
	        }
	        if(!Config.USE_ONE_SHOT_PREVIEW) {
	        	camera.setPreviewCallback(cameraHandler);	 
	        } /*else {
	        	camera.setOneShotPreviewCallback(cameraHandler);
	        }*/
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
	

	public ARToolkit getArtoolkit() {
		return artoolkit;
	}
	
	

}