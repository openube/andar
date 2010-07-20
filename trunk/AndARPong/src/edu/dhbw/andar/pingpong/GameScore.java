package edu.dhbw.andar.pingpong;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.util.GraphicsUtil;
import edu.dhbw.andarpong.R;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

/**
 * Representing the GameScore
 * @author Tobi
 *
 */
public class GameScore implements GameObject {
	
	private int computer = 0;
	private int player = 0;
	private Resources res;
	int[]  textureIDs = new int[5];
	private FloatBuffer verticesBuffer;
	private FloatBuffer normalsBuffer;
	private FloatBuffer textureCoordsBuffer;
	
	private float vertices[] = 
	{
		-1f, -1f, 0f,
		1f, -1f, 0f,
		-1f,  1f, 0f,
		1f, -1f, 0f,
		1f,  1f, 0f,
		-1f,  1f, 0f
	};
	
	private final float textureCoords[] = 
	{
			1f, 0f, 
			0f, 0f, 
			1f,  1f, 
			0f, 0f, 
			0f,  1f, 
			1f,  1f
		};
	/*{
		0f, 0f, 
		1f, 0f, 
		0f,  1f, 
		1f, 0f, 
		1f,  1f, 
		0f,  1f
	};*/
	
	private final float normals[] = 
	{
		0, 0, 1f,
		0, 0, 1f,
		0, 0, 1f,

		0, 0, 1f,
		0, 0, 1f,
		0, 0, 1f
	};
	
	private float halfWidth;
	
	public GameScore(Resources res) {
		this.res = res;
		halfWidth = (GameThread.UPPERLIMITY - GameThread.LOWERLIMITY)*0.5f*0.5f*0.333f;//width of one digit = half width of the board for all 3 digits
		float halfHeight = halfWidth*2.0f;
		vertices = new float[]
		{
				-halfWidth, -halfHeight, 0f,
				halfWidth, -halfHeight, 0f,
				-halfWidth,  halfHeight, 0f,
				halfWidth, -halfHeight, 0f,
				halfWidth,  halfHeight, 0f,
				-halfWidth,  halfHeight, 0f
		};
		verticesBuffer = GraphicsUtil.makeFloatBuffer(vertices);
		normalsBuffer = GraphicsUtil.makeFloatBuffer(normals);
		textureCoordsBuffer = GraphicsUtil.makeFloatBuffer(textureCoords);
	}

	@Override
	public void draw(GL10 gl) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);//looks cooler ;)
		//gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[4]);//colon
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);	
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBuffer);
	
		
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		gl.glTranslatef(-halfWidth*2.0f, 0, 0);		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[player]);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		gl.glTranslatef(halfWidth*4.0f, 0, 0);	
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[computer]);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
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
		gl.glGenTextures(5, textureIDs, 0);
		//load the textures into the graphics memory
		
		//colon
		//Bitmap bm = BitmapFactory.decodeResource(res, R.raw.colon);
		Bitmap bm = BitmapFactory.decodeResource(res, R.raw.colon);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[4]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		//digit 0
		bm = BitmapFactory.decodeResource(res, R.raw.digit0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 1
		bm = BitmapFactory.decodeResource(res, R.raw.digit1);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[1]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 2
		bm = BitmapFactory.decodeResource(res, R.raw.digit2);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[2]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 3
		bm = BitmapFactory.decodeResource(res, R.raw.digit3);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[3]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
	}
	
	public void incComputerScore() {
		computer++;
		if(computer>3) {
			computer = 0;
			player = 0;
		}
	}
	
	public void incPlayerScore() {
		player++;
		if(player>3) {
			computer = 0;
			player = 0;
		}
	}

}
