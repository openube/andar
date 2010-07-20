package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLBox {
	float boxvec[][] =
	{
		{-1.0f, 0.0f, 0.0f},
		{0.0f, 1.0f, 0.0f},
		{1.0f, 0.0f, 0.0f},
		{0.0f, -1.0f, 0.0f},
		{0.0f, 0.0f, 1.0f},
		{0.0f, 0.0f, -1.0f}
	};

	ShortBuffer boxndex [] = 
	{
		OpenGLUtils.toShortBuffer(new short[]{0, 1, 2}),
		OpenGLUtils.toShortBuffer(new short[]{0, 2, 3}),
		OpenGLUtils.toShortBuffer(new short[]{3, 2, 6}),
		OpenGLUtils.toShortBuffer(new short[]{3, 6, 7}),
		OpenGLUtils.toShortBuffer(new short[]{6, 4, 7}),
		OpenGLUtils.toShortBuffer(new short[]{6, 5, 4}),
		OpenGLUtils.toShortBuffer(new short[]{4, 5, 1}),
		OpenGLUtils.toShortBuffer(new short[]{4, 1, 0}),
		OpenGLUtils.toShortBuffer(new short[]{2, 1, 5}),
		OpenGLUtils.toShortBuffer(new short[]{2, 5, 6}),
		OpenGLUtils.toShortBuffer(new short[]{3, 7, 4}),
		OpenGLUtils.toShortBuffer(new short[]{3, 4, 0})
	};


	FloatBuffer vBuffer;
	
	float parms[]=new float[3];
	
	public void draw(GL10 gl,float Width, float Depth, float Height)
	{
		
		 //maybe clear buffer.
		if (vBuffer!=null) 
		{
			if (parms[0] != Width || parms[1] != Depth || parms[2] != Height) 
			{
				
				//free(v);
				//free(n);
				vBuffer = null; //maybe free later.

				gl.glVertexPointer(3, GL10.GL_FLOAT,0,OpenGLUtils.allocateFloatBuffer(0));
				
			}
		}
		
		int i;
		if(vBuffer==null){
		float v[]=new float[8*3];
		v[0*3+0] = v[1*3+0] = v[2*3+0] = v[3*3+0] = - Width/ 2.0f;
		v[4*3+0] = v[5*3+0] = v[6*3+0] = v[7*3+0] = Width / 2.0f;
		v[0*3+1] = v[1*3+1] = v[4*3+1] = v[5*3+1] = -Depth / 2.0f;
		v[2*3+1] = v[3*3+1] = v[6*3+1] = v[7*3+1] = Depth / 2.0f;
		v[0*3+2] = v[3*3+2] = v[4*3+2] = v[7*3+2] = -Height / 2.0f;
		v[1*3+2] = v[2*3+2] = v[5*3+2] = v[6*3+2] = Height / 2.0f;
		vBuffer=OpenGLUtils.toFloatBufferPositionZero(v);
		
		parms[0]=Width;
		parms[1]=Depth;
		parms[2]=Height;
		}
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vBuffer);
		gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);

		for (i = 0; i < 6; i++)
		{
			gl.glNormal3f(boxvec[i][0], boxvec[i][1], boxvec[i][2]);
			gl.glDrawElements(GL10.GL_TRIANGLES, 3, GL10.GL_UNSIGNED_SHORT, boxndex[i*2]);
			gl.glDrawElements(GL10.GL_TRIANGLES, 3, GL10.GL_UNSIGNED_SHORT, boxndex[i*2+1]);
		}

		gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
	}
	
}