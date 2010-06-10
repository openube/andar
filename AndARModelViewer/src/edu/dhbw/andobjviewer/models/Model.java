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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import edu.dhbw.andobjviewer.util.BaseFileUtil;

public class Model implements Serializable{
	//position/rotation/scale
	public float xrot = 90;
    public float yrot = 0;
    public float zrot = 0;
    public float xpos = 0;
    public float ypos = 0;
    public float zpos = 0;
    public float scale = 4f;
    public int STATE = STATE_DYNAMIC;
    public static final int STATE_DYNAMIC = 0;
    public static final int STATE_FINALIZED = 1;
    
	
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
		if(STATE == STATE_FINALIZED)
			grp.finalize();
		groups.add(grp);
	}
	
	public Vector<Group> getGroups() {
		return groups;
	}
	
	public void setFileUtil(BaseFileUtil fileUtil) {
		for (Iterator iterator = materials.values().iterator(); iterator.hasNext();) {
			Material mat = (Material) iterator.next();
			mat.setFileUtil(fileUtil);
		}
	}
	
	
	public HashMap<String, Material> getMaterials() {
		return materials;
	}

	public void setScale(float f) {
		this.scale += f;
		if(this.scale < 0.0001f)
			this.scale = 0.0001f;
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
	
	/**
	 * convert all dynamic arrays to final non alterable ones.
	 */
	public void finalize() {
		if(STATE != STATE_FINALIZED) {
			STATE = STATE_FINALIZED;
			for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
				Group grp = (Group) iterator.next();
				grp.finalize();
				grp.setMaterial(materials.get(grp.getMaterialName()));
			}
			for (Iterator<Material> iterator = materials.values().iterator(); iterator.hasNext();) {
				Material mtl = iterator.next();
				mtl.finalize();
			}
		}
	}
	
	/*
	 * get  a google protocol buffers builder, that may be serialized
	 */
	/*public BufferModel getProtocolBuffer() {
		ModelProtocolBuffer.BufferModel.Builder builder = ModelProtocolBuffer.BufferModel.newBuilder();
		
		return builder.build();
	}*/
	
}
