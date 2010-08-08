package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.ARToolkit;

/**
 * An example of an AR object being drawn on a marker.
 * @author tobi
 *
 */
public class GameCenter extends ARObject implements GameObject {

	private double[] invTrans = new double[12];
	
	private float x=0;
	private float y=0;
	private float ox=0;
	private float oy=0;
	
	public GameCenter(String name, String patternName,
			double markerWidth, double[] markerCenter) {
		super(name, patternName, markerWidth, markerCenter);		
	}
	public GameCenter(String name, String patternName,
			double markerWidth, double[] markerCenter, float[] customColor) {
		super(name, patternName, markerWidth, markerCenter);		
	}

	
	/**
	 * Everything drawn here will be drawn directly onto the marker,
	 * as the corresponding translation matrix will already be applied.
	 */
	@Override
	public final void draw(GL10 gl) {
	}
	@Override
	public void init(GL10 gl) {
	}
	
	public synchronized void transformToCenter(GL10 gl) {
		if(glCameraMatrixBuffer != null) {
			super.draw(gl);
		}
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}
	
	

	@Override
	public float getOldX() {
		return ox;
	}

	@Override
	public float getOldY() {
		return oy;
	}

	public double[] getInvTransMat() {
		return invTrans;
	}
	
	@Override
	public synchronized void update(long time) {
		if(isVisible()) {
			double[] transmat = getTransMatrix();
			ARToolkit.arUtilMatInv(transmat, invTrans);
			double marker_x = transmat[3];
			double marker_y = transmat[7];
			this.ox = x;
			this.oy = y;
			this.x = (float)marker_x;
			this.y = (float)marker_y;
		}
		
	}
}
