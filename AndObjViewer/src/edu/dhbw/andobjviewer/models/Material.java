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
package edu.dhbw.andobjviewer.models;

import java.io.Serializable;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;

import edu.dhbw.andobjviewer.util.MemUtil;

public class Material implements Serializable {
	//default values:
	//http://wiki.delphigl.com/index.php/glMaterial
	//private float[] ambientlightArr = {0.2f, 0.2f, 0.2f, 1.0f};
	//private float[] diffuselightArr = {0.8f, 0.8f, 0.8f, 1.0f};
	//private float[] specularlightArr = {0.0f, 0.0f, 0.0f, 1.0f};
	//public access for performance reasons
	public FloatBuffer ambientlight = MemUtil.makeFloatBuffer(4);
	public FloatBuffer diffuselight = MemUtil.makeFloatBuffer(4);
	public FloatBuffer specularlight = MemUtil.makeFloatBuffer(4);
	public float shininess = 0;
	
	private Bitmap texture = null;
	
	private String name;
	
	public Material(String name) {
		this.name = name;
		//fill with default values
		ambientlight.put(new float[]{0.2f, 0.2f, 0.2f, 1.0f});
		ambientlight.position(0);
		diffuselight.put(new float[]{0.8f, 0.8f, 0.8f, 1.0f});
		diffuselight.position(0);
		specularlight.put(new float[]{0.0f, 0.0f, 0.0f, 1.0f});
		specularlight.position(0);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setAmbient(float[] arr) {
		copyToBuffer(arr, ambientlight);
	}
	
	public void setDiffuse(float[] arr) {
		copyToBuffer(arr, diffuselight);
	}
	
	public void setSpecular(float[] arr) {
		copyToBuffer(arr, specularlight);
	}
	
	public void setShininess(float ns) {
		shininess = ns;
	}
	
	/**
	 * Sets the alpha value of the light sources.
	 * @param alpha
	 */
	public void setAlpha(float alpha) {
		ambientlight.put(3, alpha);
		diffuselight.put(3, alpha);
		specularlight.put(3, alpha);
	}
	
	public Bitmap getTexture() {
		return texture;
	}

	public void setTexture(Bitmap texture) {
		this.texture = texture;
	}
	
	public boolean hasTexture() {
		return this.texture != null;
	}

	/**
	 * copies a light describtion from a float array
	 * to a float buffer.
	 * 
	 * @param arr
	 * @param buff
	 */
	private static void copyToBuffer(float[]arr, FloatBuffer buff) {
		if(arr.length==4) {
			buff.put(3, arr[3]);
		} 
		if(arr.length >=3) {
			buff.put(0, arr[0]);
			buff.put(1, arr[1]);
			buff.put(2, arr[2]);
		}
	}
	
	/**
	 * stores the arrays in memory regions accessible by opengl
	 * must be done before it is being used in opengl code
	 */
//	public void finalize() {
//		ambientlight = MemUtil.makeFloatBuffer(ambientlightArr);
//		diffuselight = MemUtil.makeFloatBuffer(diffuselightArr);
//		specularlight = MemUtil.makeFloatBuffer(specularlightArr);
//	}
	
}
