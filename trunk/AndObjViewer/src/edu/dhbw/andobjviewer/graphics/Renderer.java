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

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andobjviewer.models.Vector3D;
import edu.dhbw.andobjviewer.util.MemUtil;


import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/**
 * cares about 3d rendering of the scene
 * @author tobi
 *
 */
public class Renderer implements GLSurfaceView.Renderer {
	
	private final Model3D[] models;
	private final int modelCount;
	private final Vector3D cameraPosition = new Vector3D(0, 3, 50);
	
	public Renderer(Vector<Model3D> models) {
		this.models = models.toArray(new Model3D[models.size()]);
		modelCount = this.models.length;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		//String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, cameraPosition.x, cameraPosition.y, cameraPosition.z,
				0, 0, 0, 0, 1, 0);
		
		for (int i = 0; i < modelCount; i++) {
			models[i].draw(gl);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0,0,width,height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();  
        GLU.gluPerspective(gl, 45.0f, ((float)width)/height, 0.11f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();        
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0,0,0,0);
		
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		//lighting stuff
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_LIGHTING);
		float[] ambientlight = {.3f, .3f, .3f, 1f};
		float[] diffuselight = {.7f, .7f, .7f, 1f};
		float[] specularlight = {0.6f, 0.6f, 0.6f, 1f};
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, MemUtil.makeFloatBuffer(ambientlight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, MemUtil.makeFloatBuffer(diffuselight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, MemUtil.makeFloatBuffer(specularlight));
		gl.glEnable(GL10.GL_LIGHT0);
		
		//initialize the models
		for (int i = 0; i < modelCount; i++) {
			models[i].init(gl);
		}
		
	}

}
