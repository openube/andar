package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;


import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.GraphicsUtil;
/**
 * 
 * @author tobi
 *
 */
public class GameRenderer implements OpenGLRenderer {
	
	Vector<GameObject> gameObjects = new Vector<GameObject>();
	private GameCenter center;
	
	public GameRenderer(GameCenter center) {
		this.center = center;
	}
	
	/**
	 * Light definitions
	 */	
		
	
	private float[] ambientlight1 = {.3f, .3f, .3f, 1f};
	private float[] diffuselight1 = {.7f, .7f, .7f, 1f};
	private float[] specularlight1 = {0.6f, 0.6f, 0.6f, 1f};
	private float[] lightposition1 = {0f,-40.0f,100.0f,1f};
	
	private FloatBuffer lightPositionBuffer1 =  GraphicsUtil.makeFloatBuffer(lightposition1);
	private FloatBuffer specularLightBuffer1 = GraphicsUtil.makeFloatBuffer(specularlight1);
	private FloatBuffer diffuseLightBuffer1 = GraphicsUtil.makeFloatBuffer(diffuselight1);
	private FloatBuffer ambientLightBuffer1 = GraphicsUtil.makeFloatBuffer(ambientlight1);

	
	public final void draw(GL10 gl) {
		if(center.isVisible()) {
			gl.glRotatef(-90, 0, 0, 1f);
			//draw non AR stuff here.
			for (GameObject gameObj : gameObjects) {
				gl.glPushMatrix();
				gameObj.draw(gl);
				gl.glPopMatrix();
			}
		}
	}


	public final void setupEnv(GL10 gl) {
		if(center.isVisible()) {
			center.transformToCenter(gl);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLightBuffer1);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLightBuffer1); 
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer1);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lightPositionBuffer1);
			gl.glEnable(GL10.GL_LIGHT1);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		    gl.glDisable(GL10.GL_TEXTURE_2D);		    
		    init(gl);
		}
	}
	
	private final void init(GL10 gl) {
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glEnable(GL10.GL_RESCALE_NORMAL);
	}
	
	public final void initGL(GL10 gl) {
		init(gl);
		for (GameObject gameObj : gameObjects) {
			gameObj.init(gl);
		}
	}
	
	public void addGameObject(GameObject obj) {
		gameObjects.add(obj);
	}
}
