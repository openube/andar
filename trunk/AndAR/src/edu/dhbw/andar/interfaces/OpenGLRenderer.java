package edu.dhbw.andar.interfaces;

import javax.microedition.khronos.opengles.GL10;

public interface OpenGLRenderer {
	public void draw(GL10 gl);
	public void setupEnv(GL10 gl);
	public void initGL(GL10 gl);
}
