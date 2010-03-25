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
import java.util.HashMap;
import java.util.Vector;

public class Model implements Serializable{
	//position/rotation/scale
	public float xrot = 0;
    public float yrot = 0;
    public float zrot = 0;
    public float xpos = 0;
    public float ypos = 0;
    public float zpos = 0;
    public float scale = 1f;
	
	private Vector<Group> groups = new Vector<Group>();
	/**
	 * all materials
	 */
	protected HashMap<String, Material> materials = new HashMap<String, Material>();
	
	public Model() {
		//add default material
		materials.put("default",new Material("default"));
	}
	
	public void addMaterial(Material mat) {
		//mat.finalize();
		materials.put(mat.getName(), mat);
	}
	
	public Material getMaterial(String name) {
		return materials.get(name);
	}
	
	public void addGroup(Group grp) {
		grp.finalize();
		groups.add(grp);
	}
	
	public Vector<Group> getGroups() {
		return groups;
	}
	
	
	
	public HashMap<String, Material> getMaterials() {
		return materials;
	}

	public void setScale(float f) {
		this.scale += f;
		if(this.scale < 0.01f)
			this.scale = 0.01f;
	}

	public void setXrot(float dY) {
		this.xrot += dY;
	}

	public void setYrot(float dX) {
		this.yrot += dX;
	}

	public void setXpos(float f) {
		this.xpos += f;
	}

	public void setYpos(float f) {
		this.ypos += f;
	}
	
}
