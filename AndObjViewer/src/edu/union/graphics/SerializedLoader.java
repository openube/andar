/**
	Copyright (C) 2010  Tobias Domhan

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
package edu.union.graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Log;

/**
 * loads a previously serialized model.
 * @author Tobias Domhan
 *
 */
public class SerializedLoader extends AbstractModelLoader{
	
	/**
	 * serializes the model
	 * @param model the model file that shall be serialized
	 * @param origFile the original file tha is being represented by the model
	 */
	public void serialize(Model model, File origFile) {
		File outFile = new File(origFile.getAbsolutePath()+".model");
		try {
			FileOutputStream fout = new FileOutputStream(outFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(model);
			oos.close();
			fout.close();
		} catch (FileNotFoundException e) {
			Log.e("SerializedLoader",e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("SerializedLoader",e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.union.graphics.ModelLoader#canLoad(java.io.File)
	 */
	@Override
	public boolean canLoad(File f) {
		if(f.getName().endsWith(".model")) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see edu.union.graphics.ModelLoader#load(java.io.InputStream)
	 */
	@Override
	public Model load(InputStream is) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			Model model = (Model)ois.readObject();
			ois.close();
			is.close();
			return model;
		} catch (ClassNotFoundException e) {
			Log.e("SerializedLoader",e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
