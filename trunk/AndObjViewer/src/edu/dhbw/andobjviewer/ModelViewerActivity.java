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
import edu.union.graphics.IntMesh;
import edu.union.graphics.MD2Loader;
import edu.union.graphics.Model;
import edu.union.graphics.ObjLoader;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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
		
		
		//fill list of available model loaders
        AbstractModelLoader loader = new ObjLoader();
        loader.setFactory(IntMesh.factory());
        availableModelLoaders.add(loader);
        loader = new MD2Loader();
        loader.setFactory(IntMesh.factory());
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
		/*try {
			Model model = loader.load(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
			//TODO return intent and finish acticity
			Toast.makeText(this, e.getMessage(), TOAST_TIMEOUT).show();
		}*/
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
}
