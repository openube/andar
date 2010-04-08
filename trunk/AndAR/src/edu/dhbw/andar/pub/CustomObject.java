package edu.dhbw.andar.pub;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.ARObject;

/**
 * An example of an AR object being drawn on a marker.
 * @author tobi
 *
 */
public class CustomObject extends ARObject {
	
	public CustomObject(String name, String patternName,
			double markerWidth, double[] markerCenter) {
		super(name, patternName, markerWidth, markerCenter);
	}
	
	/**
	 * Everything drawn here will be drawn directly onto the marker,
	 * as the corresponding translation matrix will already be applied.
	 */
	@Override
	public final void draw(GL10 gl) {
		super.draw(gl);
	}
}
