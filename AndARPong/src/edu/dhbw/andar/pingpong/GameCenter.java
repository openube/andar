package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;

import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.AndARRenderer;
import edu.dhbw.andar.util.GraphicsUtil;

/**
 * An example of an AR object being drawn on a marker.
 * @author tobi
 *
 */
public class GameCenter extends ARObject implements GameObject {

	private double trans0=0;
	private double trans1=0;
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
	
	public double transform1DX(double val) {
		return trans0*val+trans1*val;
	}
	
	@Override
	public void update(long time) {
		double[] transmat = getTransMatrix();
		trans0 = transmat[0];
		trans1 = transmat[1];
		double marker_x = transmat[3];
		double marker_y = transmat[7];
		this.ox = x;
		this.oy = y;
		this.x = (float)marker_x;
		this.y = (float)marker_y;
		double winkel = Math.acos(transmat[0]);
		Log.d("CENTER_WINKEL",winkel+"");
	}
}
