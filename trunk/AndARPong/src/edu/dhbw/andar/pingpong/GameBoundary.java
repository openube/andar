package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

public class GameBoundary implements GameObject {
	
	private final float depth = 5.0f;
	private final float height = 20.0f;
	private float width;
	private float y;
	private float z;
	
	
	public GameBoundary(float yPos) {
		width = GameThread.UPPERLIMITX - GameThread.LOWERLIMITX;
		y = (yPos > 0) ? yPos + depth / 2.0f : yPos - depth / 2.0f;
		z = height / 2.0f;
	}

	@Override
	public void draw(GL10 gl) {		
		gl.glTranslatef(0f, y, z);		
		//x, y, z??
		GLUT.glutSolidBox(gl, width , depth, height);
		gl.glScalef(width, depth, height);
		GLUT.glutSolidCube(gl, 1f);		
	}

	@Override
	public float getOldX() {
		return 0;
	}

	@Override
	public float getOldY() {
		return 0;
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public void update(long time) {		
	}

	@Override
	public void init(GL10 gl) {
	}

}
