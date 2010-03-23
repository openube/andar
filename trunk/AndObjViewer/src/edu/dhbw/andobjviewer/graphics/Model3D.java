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
package edu.dhbw.andobjviewer.graphics;

import java.io.Serializable;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andobjviewer.models.Group;
import edu.dhbw.andobjviewer.models.Material;
import edu.dhbw.andobjviewer.models.Model;


/**
 * represents a 3d model.
 * @author tobi
 *
 */
public class Model3D implements Serializable{
	
	private Model model;
	private final Group[] groups;
	private final int groupCount; 
	private HashMap<String, Material> materials;
	
	public Model3D(Model model) {
		this.model = model;
		groups = model.getGroups().toArray(new Group[model.getGroups().size()]);
		groupCount = groups.length;
		materials = model.getMaterials();
	}
	
	public void init(GL10 gl) {
		//transfer vertices to video memory
	}
	
	public void draw(GL10 gl) {
		//do positioning:
		gl.glScalef(model.scale, model.scale, model.scale);
		gl.glTranslatef(model.xpos, model.ypos, model.zpos);
		gl.glRotatef(model.xrot, 1, 0, 0);
		gl.glRotatef(model.yrot, 0, 1, 0);
		gl.glRotatef(model.zrot, 0, 0, 1);
		
		//tmp
		gl.glColor4f(0f, 1f, 0f, 1f);
		//end tmp
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		
		
		//draw each groups
		for (int i = 0; i < groupCount; i++) {
			Group group = groups[i];
			Material mat = materials.get(group.getMaterial());
			if(mat != null) {
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, mat.specularlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat.ambientlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat.diffuselight);
				gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat.shininess);
			}
			gl.glVertexPointer(3,GL10.GL_FLOAT, 0, group.vertices);
	        gl.glNormalPointer(GL10.GL_FLOAT,0, group.normals);
	        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, group.vertexCount);
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}
}
