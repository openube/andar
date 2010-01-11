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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andobjviewer.graphics.Model3D;
import edu.union.graphics.Mesh;
import edu.union.graphics.Model;

import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;

/**
 * renders the given model
 * @author Tobias Domhan
 *
 */
public class ModelRenderer implements Renderer {
	/**
	 * the model that shall be displayed
	 */
	private Model3D model;
	private final Vector3D cameraPosition = new Vector3D(0, 3, 5);
	
	/**
	 * Light definitions
	 */
	private float[] ambientlight = {.3f, .3f, .3f, 1f};
	private float[] diffuselight = {.7f, .7f, .7f, 1f};
	private float[] specularlight = {0.6f, 0.6f, 0.6f, 1f};
	private float[] specref = {0.99f,	0.94f,	0.81f,	1.0f};
	private float[] difcref = {0.78f,	0.57f,	0.11f,	1.0f};
	private float[] ambref = {	0.33f,	0.22f,	0.03f,	1.0f};
	
	/**
	 * 
	 */
	public ModelRenderer(Model3D model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(),
				0, 0, 0, 0, 1, 0);

        /*GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0);
        gl.glTranslatef(0,0,-10);
        gl.glRotatef(30.0f, 1, 0, 0);*/
        
        //model.draw(gl);
        
        //NEHE:
        //gl.glTranslatef(0.0f, -1.2f, -6.0f);
        //gl.glTranslatef(0.0f, 2.5f, 0.0f);       
        //gl.glTranslatef(0, -3, 0);

		
		//gl.glColor4f(0f, 1f, 0f, 1f);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, makeFloatBuffer(specref));
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, makeFloatBuffer(ambref));
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, makeFloatBuffer(difcref));
		gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 27.9f);
		model.draw(gl);
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0,0,width,height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();  
        GLU.gluPerspective(gl, 45.0f, ((float)width)/height, 0.11f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();        
	}

	/* (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0,0,0,0);
		gl.glShadeModel(GL10.GL_SMOOTH);
		
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		//enable textures:
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_CULL_FACE);
		
		//lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, makeFloatBuffer(ambientlight));
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, makeFloatBuffer(diffuselight));
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, makeFloatBuffer(specularlight));
		gl.glEnable(gl.GL_LIGHT0);
		
		//enable color tracking
		//gl.glEnable(gl.GL_COLOR_MATERIAL);
		//disable color tracking:
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		//load the model
		model.init(gl);
	}
	
	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}


}
