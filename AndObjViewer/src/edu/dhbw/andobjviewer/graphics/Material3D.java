package edu.dhbw.andobjviewer.graphics;

import edu.dhbw.andobjviewer.models.Material;

public class Material3D extends Material {
	public Material3D(String name) {
		super(name);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -5293457990367242395L;
	private int textureID=0;
	public int getTextureID() {
		return textureID;
	}
	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}
	
}
