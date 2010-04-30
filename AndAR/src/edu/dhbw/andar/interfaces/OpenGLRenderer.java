package edu.dhbw.andar.interfaces;

import javax.microedition.khronos.opengles.GL10;

/**
 * 
 * @author Tobias Domhan
 *
 */
public interface OpenGLRenderer {
	/**
	 * Draw stuff in this method that has nothing to do with Augmented Reality.
	 * Will be invoked at the end of each render phase.
	 * @param gl
	 */
	public void draw(GL10 gl);
	/**
	 * Setup the OpenGL environment. This method will be called just before
	 * each AR object is drawn. This method may be used to setup lighting, 
	 * and other things common to all Augmented Reality objects.
	 * @param gl
	 */
	public void setupEnv(GL10 gl);
	/**
	 * Called once, when the OpenGL Surface was created. Used to do some general
	 * OpenGL specific initialization.
	 * @param gl
	 */
	public void initGL(GL10 gl);
}
