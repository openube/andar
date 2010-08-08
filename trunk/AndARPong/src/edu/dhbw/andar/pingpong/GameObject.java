package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

/**
 * An Object of the game.
 * Providing X and Y coordinates both the current ones, and the ones of the last time step.
 * Furthermore it offers methods for OpenGL draw stuff and an update method that cares
 * about changes of the coordinates according to it's speed.
 * @author Tobi
 *
 */
public interface GameObject {
	//coordinates:
	public float getX();
	public float getY();
	public float getOldX();
	public float getOldY();
	//draw stuff:
	public void draw(GL10 gl);
	public void init(GL10 gl);
	//update the state/coordinates according the the time delta
	public void update(long time);
}
