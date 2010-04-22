package edu.dhbw.andobjviewer.graphics;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.OpenGLCamView;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.GraphicsUtil;
import edu.dhbw.andobjviewer.parser.Util;

public class LightingRenderer implements OpenGLRenderer{
	
	private float[] ambientlight = {1f, 1f, 1f, 1f};
	private float[] diffuselight = {1f, 1f, 1f, 1f};
	private float[] specularlight = {1f, 1f, 1f, 1f};
	private float[] lightposition ={ 100.0f,-200.0f,200.0f,0.0f};//{.0f,.0f,-1.0f,0.0f};
	
	private FloatBuffer lightPositionBuffer =  GraphicsUtil.makeFloatBuffer(lightposition);
	private FloatBuffer specularLightBuffer = GraphicsUtil.makeFloatBuffer(specularlight);
	private FloatBuffer diffuseLightBuffer = GraphicsUtil.makeFloatBuffer(diffuselight);
	private FloatBuffer ambientLightBuffer = GraphicsUtil.makeFloatBuffer(ambientlight);

	@Override
	public final void draw(GL10 gl) {
		
	}

	@Override
	public final void setupLighting(GL10 gl) {
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer);
		//gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, GraphicsUtil.makeFloatBuffer(new float[]{0f,0}));
		gl.glEnable(GL10.GL_LIGHT1);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_AMBIENT, ambientLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_DIFFUSE, diffuseLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, GraphicsUtil.makeFloatBuffer(new float[]{0f,0f,-1f,0}));
		gl.glEnable(GL10.GL_LIGHT2);
	}

}
