package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


import android.media.MediaPlayer;
import android.util.Log;

import edu.dhbw.andar.pingpong.GLUT.SolidSphere;
import edu.dhbw.andar.util.GraphicsUtil;

public class Ball implements GameObject{
	private GameCenter center;
	public final float radius = 10f;
	private float z = radius / 2.0f;
	private float x=0;
	private float y=0;
	private float ox=0;
	private float oy=0;
	private double accelerationX = 1.0f;
	private double accelerationY = 1.0f;
	private final double INITIAL_SPEED = 0.3;
	private double speed = INITIAL_SPEED;
	
	private final double acceleration = 0.00001;
	/**
	 * velocity x
	 */
	private double vx=0.0000001;
	/**
	 * velocity y
	 */
	private double vy=0.00000003;//0.00000001;
	
	/**
	 * 3d stuff
	 */
	
	private FloatBuffer mat_flash;
	private FloatBuffer mat_ambient;
	private FloatBuffer mat_flash_shiny;
	private FloatBuffer mat_diffuse;
	
	private SoundEngine soundEngine;
	
	private Random random = new Random(System.nanoTime());
	
	public Ball(GameCenter center, SoundEngine soundEngine) {
		this.center = center;
		float[] mat_ambientf     = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
		float[]mat_flashf      = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
		float[] mat_diffusef      = new float[]{0.5f, 0.5f,0.5f, 1.0f};
		float[] mat_flash_shinyf = new float[]{0.5f, 0.5f, 0.5f, 1.0f};

		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
		
		this.soundEngine = soundEngine;
		
	}
	
	/**
	 * draw the ball
	 * @param gl
	 */
	public void draw(GL10 gl) {
		if(center.isVisible()) {
			Log.d("BALL", y+"");
			
			GL11 gl11 = (GL11)gl;
			
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);
			gl.glTranslatef(x, y, z);		
			
			SolidSphere.draw(gl, radius, 12, 12);
		}
	}

	/**
	 * update the current position
	 */
	@Override
	public void update(long time) {
		time /= 1000000.0;//allows the acceleration and velocity to be bigger, in order to avoid IEEE754 format conflicts
		ox = x;
		oy = y;
		x += time * vx;
		y += time * vy;
		//check limits
		if(y+radius>GameThread.UPPERLIMITY) {
			y = GameThread.UPPERLIMITY-radius;
			bounceY();
		} else if (y-radius<GameThread.LOWERLIMITY) {
			y = GameThread.LOWERLIMITY+radius;
			bounceY();
		}		
		//increase the speed
		/*speed += acceleration;
		vy = ((vy>0) ? factorSpeedY : -factorSpeedY) * speed;
		vx = ((vx>0) ? factorSpeedX : -factorSpeedX) * speed;*/
		
		if(vx > 0)
			vx += accelerationX*time;
		else 
			vx -= accelerationX*time;
		if(vy > 0)
			vy += accelerationY*time;
		else 
			vy -= accelerationY*time;
	}
	
	
	public void reset() {
		speed = INITIAL_SPEED;
		ox = 0;
		oy = 0;
		x = 0;
		y = 0;
		//alpha ca. between 0.4 and 0.8, 0.4 approx. being PI/8
		double alpha=0.0;
		//while (alpha < 0.4) alpha = random.nextDouble();
		alpha = random.nextDouble()*0.4+0.4;
		if(random.nextBoolean())
			alpha *= -1;
		double factorSpeedY = Math.sin(alpha);
		vy = factorSpeedY * speed;
		double factorSpeedX = Math.cos(alpha);
		vx = factorSpeedX * speed;
		accelerationX = acceleration * factorSpeedX;
		accelerationY = acceleration * factorSpeedY;
	}

	public double getVx() {
		return vx;
	}

	public double getVy() {
		return vy;
	}

	@Override
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
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
	
	public void bounceY() {
		soundEngine.playBallBounceSound();
		vy*=-1.0;
	}
	
	public void bounceX() {
		soundEngine.playBallBounceSound();
		vx*=-1.0;
	}
	
	public void bounceX(double drift) {
		bounceX();
		vy += drift;
	}

	@Override
	public void init(GL10 gl) {
	}
	
}
