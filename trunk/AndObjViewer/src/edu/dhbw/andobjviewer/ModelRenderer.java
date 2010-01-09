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

import edu.union.graphics.Mesh;
import edu.union.graphics.Model;
import edu.union.graphics.Model3D;

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
        /*GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0);
        gl.glTranslatef(0,0,-10);
        gl.glRotatef(30.0f, 1, 0, 0);*/
        
        //model.draw(gl);
        
        //NEHE:
        gl.glTranslatef(0.0f, -1.2f, -6.0f);
        gl.glTranslatef(0.0f, 2.5f, 0.0f);       
        gl.glTranslatef(0, -3, 0);
        float vertices[] = { 
				0.0f, 1.0f, 0.0f, 	//Top
				-1.0f, -1.0f, 0.0f, //Bottom Left
				1.0f, -1.0f, 0.0f 	//Bottom Right
								};
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		//Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		//Enable vertex buffer
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glColor4f(0f, 1f, 0f, 1f);
		//Draw the vertices as triangle strip
		//gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		//gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
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
		//gl.glEnable(GL10.GL_CULL_FACE);
		
		//load the model
		model.init(gl);


	}

}
