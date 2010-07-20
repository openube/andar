package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

public interface GameObject {
	public float getX();
	public float getY();
	public float getOldX();
	public float getOldY();
	public void draw(GL10 gl);
	public void init(GL10 gl);
	public void update(long time);
}
