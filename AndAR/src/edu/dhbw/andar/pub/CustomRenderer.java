package edu.dhbw.andar.pub;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import edu.dhbw.andar.AndARRenderer;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.GraphicsUtil;
/**
 * 
 * @author tobi
 *
 */
public class CustomRenderer implements OpenGLRenderer {
	
	/**
	 * Light definitions
	 */
	private float[] ambientlight = {.3f, .3f, .3f, 1f};
	private float[] diffuselight = {.7f, .7f, .7f, 1f};
	private float[] specularlight = {0.6f, 0.6f, 0.6f, 1f};
	
	public final void draw(GL10 gl) {
		//draw non AR stuff here.
	}

	@Override
	public void setupLighting(GL10 gl) {
	
	}

	@Override
	public void initGL(GL10 gl) {
		//lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, GraphicsUtil.makeFloatBuffer(ambientlight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, GraphicsUtil.makeFloatBuffer(diffuselight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, GraphicsUtil.makeFloatBuffer(specularlight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, GraphicsUtil.makeFloatBuffer(new float[]{100.0f,-200.0f,200.0f,0.0f}));
		gl.glEnable(GL10.GL_LIGHT0);
		
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_CULL_FACE);
	}
}
