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

import java.io.File;
import java.io.IOException;

import edu.dhbw.andar.util.IO;
import edu.dhbw.andopenglcam.R;

import android.app.Activity;
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

public class OpenGLCamActivity extends Activity implements Callback{
	private GLSurfaceView glSurfaceView;
	private Camera camera;
	private OpenGLCamRenderer renderer;
	private Resources res;
	private CameraPreviewHandler cameraHandler;
	private boolean mPreviewing = false;
	private boolean mPausing = false;
	private MarkerInfo markerInfo = new MarkerInfo();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen();
        disableScreenTurnOff();
        //orientation is set via the manifest

        res = getResources();  
        IO.transferFilesToSDCard(res);
        glSurfaceView = new OpenGLCamView(this);
		renderer = new OpenGLCamRenderer(res, markerInfo);
		cameraHandler = new CameraPreviewHandler(glSurfaceView, renderer, res, markerInfo);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.getHolder().addCallback(this);
        setContentView(glSurfaceView);
        if(Config.DEBUG)
        	Debug.startMethodTracing("AndAR");
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
	        //params.setPreviewFrameRate(10);//TODO remove restriction
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
	        } else {
	        	camera.setOneShotPreviewCallback(cameraHandler);
	        }
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, CameraPreviewHandler.MODE_RGB, 0, res.getText(R.string.mode_rgb));
	    menu.add(0, CameraPreviewHandler.MODE_GRAY, 0, res.getText(R.string.mode_gray));
	    menu.add(0, CameraPreviewHandler.MODE_BIN, 0, res.getText(R.string.mode_bin));
	    menu.add(0, CameraPreviewHandler.MODE_EDGE, 0, res.getText(R.string.mode_edges));
	    menu.add(0, CameraPreviewHandler.MODE_CONTOUR, 0, res.getText(R.string.mode_contours));   
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		this.cameraHandler.setMode(item.getItemId());
		return true;
	}

}