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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import edu.union.graphics.AbstractModelLoader;
import edu.union.graphics.FloatMesh;
import edu.union.graphics.IntMesh;
import edu.union.graphics.MD2Loader;
import edu.union.graphics.Model;
import edu.union.graphics.Model3D;
import edu.union.graphics.ObjLoader;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

/**
 * loads a 3D Model and displays it
 * the URI of the model file has to be passed through the intent
 * 
 * @author Tobias Domhan
 *
 */
public class ModelViewerActivity extends Activity {
	/**
	 * Constants:
	 */
	private final int TOAST_TIMEOUT = 3;
	
	private GLSurfaceView modelView;
	private ModelRenderer renderer;
	private Model3D model3D;

	
	private ArrayList<AbstractModelLoader> availableModelLoaders = new ArrayList<AbstractModelLoader>();
	private Resources res;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.res = this.getResources();
		Intent intent = getIntent();
		File modelFile =  new File(URI.create(intent.getDataString()));
		
		//create the opengl view
		modelView = new GLSurfaceView(this);

		
		//fill list of available model loaders
        AbstractModelLoader loader = new ObjLoader();
        loader.setFactory(FloatMesh.factory());
        availableModelLoaders.add(loader);
        loader = new MD2Loader();
        loader.setFactory(FloatMesh.factory());
        availableModelLoaders.add(loader);
        
        
        loader = getModelloaderForFile(modelFile);
		if (loader == null) {
			//no loader available
			Intent resIntent = new Intent();
			resIntent.putExtra("error_message", res.getText(R.string.unknown_file_type));
			setResult(Activity.RESULT_CANCELED, resIntent);
			//return
			finish();
		}
		//load and view model 
		try {
			Model model = loader.load(modelFile);
			model3D = new Model3D(model);
			renderer = new ModelRenderer(model3D);
			modelView.setRenderer(renderer);
			modelView.setOnTouchListener(new EventHandler());
		} catch (IOException e) {
			e.printStackTrace();
			//TODO return intent and finish acticity
			Toast.makeText(this, e.getMessage(), TOAST_TIMEOUT).show();
		}
		setContentView(modelView);		
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		modelView.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		modelView.onPause();
	}
	
	/**
     * Iterate through all known model loaders and return a model loader that
     *  can handle the file
     * @param file
     * @return
     */
    private AbstractModelLoader getModelloaderForFile(File file) {
    	for (AbstractModelLoader loader : availableModelLoaders) {
			if (loader.canLoad(file)) {
				return loader;
			}
		}
    	return null;
    }
    
    /**
     * Handles touch events.
     * @author Tobias Domhan
     *
     */
    class EventHandler implements OnTouchListener {
    	
    	private float lastX=0;
    	private float lastY=0;

		/* (non-Javadoc)
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
					model3D.setXrot(dY);//dY-> Rotation um die X-Achse
					model3D.setYrot(dX);//dX-> Rotation um die Y-Achse
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
    
}
