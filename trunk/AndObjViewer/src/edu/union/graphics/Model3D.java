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
package edu.union.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * represents a {@link Model} 3D Model
 * @author Tobias Domhan
 *
 */
public class Model3D {
	private Model model;
	private FloatBuffer vertices;
    private FloatBuffer normals;
    private FloatBuffer texCoords;
    private int textureName;
    private float xrot = 0;
    private float yrot = 0;
    private float zrot = 0;
    private float xpos = 0;
    private float ypos = 0;
    private float zpos = 0;
    private float scale = 1f;



	/**
	 * default constructor
	 */
	public Model3D(Model model) {
		this.model = model;
	}
	
	public void init(GL10 gl) {
        Mesh ms = model.getFrame(0).getMesh();
        int verts = ms.getFaceCount()*3;

        ByteBuffer bb = ByteBuffer.allocateDirect(verts*3*4);//4 byte per float
        bb.order(ByteOrder.nativeOrder());
        vertices = bb.asFloatBuffer();

        for (int f=0;f<ms.getFaceCount();f++) {
                int[] face = ms.getFace(f);
                //TODO: faces may have more than 3 vertices.
                for (int j=0;j<3;j++) {
                        float[] v = ms.getVertexf(face[j]);
                        for (int k=0;k<3;k++) {
                                vertices.put(v[k]);
                        }
                }
        }
        vertices.position(0);
        /*if (ms.getTextureFile() != null) {
                gl.glEnable(GL10.GL_TEXTURE_2D);
                textureName = loadTexture(gl, BitmapFactory.decodeResource(c.getResources(),R.drawable.skin));
        }*/
        
        //bb = ByteBuffer.allocateDirect(msh.getVertexCount()*3*4);
        /*bb = ByteBuffer.allocateDirect(verts*3*4);
        bb.order(ByteOrder.nativeOrder());
        normals = bb.asFloatBuffer();

        
        bb = ByteBuffer.allocateDirect(verts*2*4);
        bb.order(ByteOrder.nativeOrder());
        texCoords = bb.asFloatBuffer();*/

	}
	
	/**
	 * load a bitmap as an opengl texture and return the texture name
	 * @param gl
	 * @param bmp
	 * @return
	 */
	protected static int loadTexture(GL10 gl, Bitmap bmp) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		int[] tmp_tex = new int[1];

        gl.glGenTextures(1, tmp_tex, 0);
        int tx = tmp_tex[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, tx);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);        
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        return tx;
	}

	private float rot;
	public void draw(GL10 gl) {
        /*GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0);
        gl.glTranslatef(0,0,-10);
        gl.glRotatef(30.0f, 1, 0, 0);*/
        gl.glColor4f(0f, 1f, 0f, 1f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        /*gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);*/
        gl.glScalef(scale, scale, scale);
        gl.glTranslatef(xpos, ypos, zpos);
        gl.glRotatef(xrot, 1, 0, 0);
        gl.glRotatef(yrot, 0, 1, 0);
        gl.glRotatef(zrot, 0, 0, 1);
        gl.glVertexPointer(3,GL10.GL_FLOAT, 0, vertices);
        int faces = model.getFrame(0).getMesh().getFaceCount();
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, faces*3);
	}
	
	//getters and setters:

	/**
	 * @return the xpos
	 */
	public float getXpos() {
		return xpos;
	}

	/**
	 * @param xpos the xpos to set
	 */
	public void setXpos(float xpos) {
		this.xpos += xpos;
	}

	/**
	 * @return the ypos
	 */
	public float getYpos() {
		return ypos;
	}

	/**
	 * @param ypos the ypos to set
	 */
	public void setYpos(float ypos) {
		this.ypos += ypos;
	}

	/**
	 * @return the zpos
	 */
	public float getZpos() {
		return zpos;
	}

	/**
	 * @param zpos the zpos to set
	 */
	public void setZpos(float zpos) {
		this.zpos = zpos;
	}

	/**
	 * @return the xrot
	 */
	public float getXrot() {
		return xrot;
	}

	/**
	 * @param xrot the xrot to set
	 */
	public void setXrot(float xrot) {
		this.xrot += xrot;
	}

	/**
	 * @return the yrot
	 */
	public float getYrot() {
		return yrot;
	}

	/**
	 * @param yrot the yrot to set
	 */
	public void setYrot(float yrot) {
		this.yrot += yrot;
	}

	/**
	 * @return the zrot
	 */
	public float getZrot() {
		return zrot;
	}

	/**
	 * @param zrot the zrot to set
	 */
	public void setZrot(float zrot) {
		this.zrot = zrot;
	}
	
	/**
	 * @return the scale
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(float scale) {
		this.scale += scale;
		if(this.scale < 0.01f) {
			this.scale = 0.01f;
		}
	}
	
}
