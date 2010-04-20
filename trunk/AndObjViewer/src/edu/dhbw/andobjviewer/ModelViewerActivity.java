/**
	Copyright (C) 2009  Tobias Domhan

    This file is part of AndObjViewer.

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
package edu.dhbw.andobjviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.dhbw.andobjviewer.graphics.Model3D;
import edu.dhbw.andobjviewer.graphics.Renderer;
import edu.dhbw.andobjviewer.models.Model;
import edu.dhbw.andobjviewer.parser.ObjParser;
import edu.dhbw.andobjviewer.parser.ParseException;
import edu.dhbw.andobjviewer.parser.Util;
import edu.dhbw.andobjviewer.util.FileUtil;

/**
 * loads a 3D Model and displays it
 * the URI of the model file has to be passed through the intent
 * 
 * @author Tobias Domhan
 *
 */
public class ModelViewerActivity extends Activity{
	
	private GLSurfaceView modelView;
	private Renderer renderer;
	private Model model;
	
	public static final boolean DEBUG = true;
	
	/* Menu Options: */
	private final int MENU_SCALE = 0;
	private final int MENU_ROTATE = 1;
	private final int MENU_TRANSLATE = 2;
	
	private int mode = MENU_TRANSLATE;
	ProgressDialog waitDialog;
	Vector<Model3D> models = new Vector<Model3D>();

	
	private Resources res;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		//create a wait dialog
		waitDialog = ProgressDialog.show(this, "", 
                getResources().getText(R.string.loading), true);
		waitDialog.show();
		if(model == null) {
			new ModelLoader().execute();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(modelView != null)
			modelView.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(modelView != null)
			modelView.onPause();
	}
    
    
    
    /* create the menu
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_TRANSLATE, 0, res.getText(R.string.translate))
    		.setIcon(R.drawable.translate);
        menu.add(0, MENU_ROTATE, 0, res.getText(R.string.rotate))
        	.setIcon(R.drawable.rotate);
        menu.add(0, MENU_SCALE, 0, res.getText(R.string.scale))
        	.setIcon(R.drawable.scale);        
        return true;
    }
    

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_SCALE:
	            mode = MENU_SCALE;
	            return true;
	        case MENU_ROTATE:
	        	mode = MENU_ROTATE;
	            return true;
	        case MENU_TRANSLATE:
	        	mode = MENU_TRANSLATE;
	            return true;
        }
        return false;
    }
    
    /* saving avtivity state:
     * http://developer.android.com/guide/topics/fundamentals.html#actstate
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	//save the model
    	//outState.putSerializable("model", model);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);  
    	//model = (Model) savedInstanceState.getSerializable("model");
    }
    
    /**
     * Handles touch events.
     * @author Tobias Domhan
     *
     */
    class TouchEventHandler implements OnTouchListener {
    	
    	private float lastX=0;
    	private float lastY=0;

		/* handles the touch events.
		 * the object will either be scaled, translated or rotated, dependen on the
		 * current user selected mode.
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
		 */
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()) {
				//Action started
				default:
				case MotionEvent.ACTION_DOWN:
					lastX = event.getX();
					lastY = event.getY();
					break;
				//Action ongoing
				case MotionEvent.ACTION_MOVE:
					float dX = lastX - event.getX();
					float dY = lastY - event.getY();
					lastX = event.getX();
					lastY = event.getY();
					if(model != null) {
						switch(mode) {
							case MENU_SCALE:
								model.setScale(dY/-100);
					            break;
					        case MENU_ROTATE:
					        	model.setXrot(-1*dY);//dY-> Rotation um die X-Achse
								model.setYrot(-1*dX);//dX-> Rotation um die Y-Achse
					            break;
					        case MENU_TRANSLATE:
					        	model.setXpos(dX/-100f);
								model.setYpos(dY/100f);
					        	break;
						}		
					}
					break;
				//Action ended
				case MotionEvent.ACTION_CANCEL:	
				case MotionEvent.ACTION_UP:
					lastX = event.getX();
					lastY = event.getY();
					break;
			}
			return true;
		}
    	
    }
    
    private class ModelLoader extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected Void doInBackground(Void... params) {
    		//read the model file:
			res = getResources();
			Intent intent = getIntent();
			File modelFile =  new File(URI.create(intent.getDataString()));
			
			FileUtil fileUtil = new FileUtil();
			fileUtil.setBaseFolder(modelFile.getParentFile());
			
			if(modelFile.getAbsolutePath().endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
					Debug.startMethodTracing("AndObjViewer");
					BufferedReader modelFileReader = new BufferedReader(new FileReader(modelFile));
					String shebang = modelFileReader.readLine();				
					if(!shebang.equals("#trimmed")) {
						//trim the file:			
						File trimmedFile = new File(modelFile.getAbsolutePath()+".tmp");
						BufferedWriter trimmedFileWriter = new BufferedWriter(new FileWriter(trimmedFile));
						Util.trim(modelFileReader, trimmedFileWriter);
						if(modelFile.delete()) {
							trimmedFile.renameTo(modelFile);
							modelFile =  new File(URI.create(intent.getDataString()));
						}					
					}
					model = parser.parse("Model", new BufferedReader(new FileReader(modelFile)));
					models.add(new Model3D(model));
					Debug.stopMethodTracing();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
    		return null;
    	}
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		waitDialog.dismiss();
    		//create the opengl view
    		modelView = new GLSurfaceView(ModelViewerActivity.this);	
    		    		
    		renderer = new Renderer(models);
    		modelView.setRenderer(renderer);
    		modelView.setOnTouchListener(new TouchEventHandler());
    		setContentView(modelView);
    	}
    }
    
}
