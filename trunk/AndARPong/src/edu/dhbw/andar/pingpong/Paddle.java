package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;

import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.AndARRenderer;
import edu.dhbw.andar.pingpong.GLUT.SolidBox;
import edu.dhbw.andar.util.GraphicsUtil;

/**
 * An example of an AR object being drawn on a marker.
 * @author tobi
 *
 */
public abstract class Paddle implements GameObject {
	private final float height = 20.0f;
	private final float depth = 5.0f;
	private final float z = height /2.0f;
	
	
	protected float x=0;
	protected float y=0;
	protected float ox=0;
	protected float oy=0;
	protected final float width = 50f;
	protected final float halfWidth = width / 2.0f;

	protected int ID;
	protected  GameCenter center;

	
	public Paddle(int ID, GameCenter center) {
		this.ID = ID;
		
		this.center = center;

		float   mat_ambientf[]=null;
		float   mat_flashf[]=null;
		float   mat_diffusef[]=null;
		float   mat_flash_shinyf[]=null;
		mat_ambientf     = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
		mat_flashf      = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
		mat_diffusef      = new float[]{0.5f, 0.5f,0.5f, 1.0f};
		mat_flash_shinyf = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
		switch(ID) {
		default:
		case 0:
			x =  GameThread.UPPERLIMITX + depth / 2.0f;	
			break;
		case 1:
			x =  GameThread.LOWERLIMITX - depth / 2.0f;			
			break;
		}

		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
		
	}
	
	private OpenGLBox box = new OpenGLBox();
	private FloatBuffer mat_flash;
	private FloatBuffer mat_ambient;
	private FloatBuffer mat_flash_shiny;
	private FloatBuffer mat_diffuse;
	
	/**
	 * Everything drawn here will be drawn directly onto the marker,
	 * as the corresponding translation matrix will already be applied.
	 */
	@Override
	public final void draw(GL10 gl) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);

	    //draw cube
			    
	    gl.glTranslatef( x, y + halfWidth, z);
	    
	    
	    box.draw(gl, depth, width, height);
	}

	protected void checkBoundaries() {
		y = (float)((y+width > GameThread.UPPERLIMITY) ? GameThread.UPPERLIMITY - width : (y < GameThread.LOWERLIMITY) ? GameThread.LOWERLIMITY : y);
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
	public abstract void  update(long time);

	@Override
	public float getOldX() {
		return ox;
	}

	@Override
	public float getOldY() {
		return oy;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public double getWidth() {
		return width;
	}
	
}
