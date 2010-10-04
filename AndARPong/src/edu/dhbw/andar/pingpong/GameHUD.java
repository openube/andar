package edu.dhbw.andar.pingpong;

import java.io.Writer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.debug.LogWriter;
import edu.dhbw.andar.util.GraphicsUtil;
import edu.dhbw.andarpong.R;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLDebugHelper;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Representing the GameScore.
 * Displaying the score and if a player won the game.
 * @author Tobi
 *
 */
public class GameHUD implements GameObject {
	
	private GameScore score;
	
	private Resources res;
	int[]  textureIDs = new int[6];
	private FloatBuffer verticesBufferDigits;
	private FloatBuffer normalsBuffer;
	private FloatBuffer textureCoordsBufferDigits;
	private FloatBuffer verticesBufferText;
	private FloatBuffer[] textureCoordsBuffers = new FloatBuffer[2];
	
	//display each message for 2 seconds
	private final static long eventDisplayTime = 2000;
	private long startDispTime=0;
	
	private final int STATE_DISP_NOTHING=2;
	private final int STATE_DISP_WON=1;
	private final int STATE_DISP_LOST=0;
	private int state = STATE_DISP_NOTHING;
	
	private final int TEXTURE_ID_TEXTS = 5;
	
	private float verticesDigits[] = 
	{
		-1f, -1f, 0f,
		1f, -1f, 0f,
		-1f,  1f, 0f,
		1f, -1f, 0f,
		1f,  1f, 0f,
		-1f,  1f, 0f
	};
	
	private float verticesText[];
	
	private final float textureCoords[] = 
	{
			0.5f, 0f, 
			0f, 0f, 
			0.5f,  1f, 
			0f, 0f, 
			0f,  1f, 
			0.5f,  1f
		};
	private final float textureCoordsTextWon[] = 
	{
			1f, 0f, 
			0f, 0f, 
			1f,  0.3f, 
			0f, 0f, 
			0f,  0.3f, 
			1f,  0.3f, 
		};
	private final float textureCoordsTextLost[] = 
	{
			1f,  0.3f, 
			0f,  0.3f, 
			1f,  0.57f, 
			0f,  0.3f,  
			0f,  0.57f, 
			1f,  0.57f, 
		};
	
	private final float normals[] = 
	{
		0, 0, 1f,
		0, 0, 1f,
		0, 0, 1f,

		0, 0, 1f,
		0, 0, 1f,
		0, 0, 1f
	};
	
	private float digitWidth;
	private float digitHeight;
	/**
	 * Padding between the digits and the text.
	 */
	private final float textPadding=20.0f; 
	
	public GameHUD(Resources res, GameScore score) {
		//initiate all vertex/coordinate buffers
		this.res = res;
		this.score = score;
		digitWidth = (GameThread.UPPERLIMITY - GameThread.LOWERLIMITY)*0.5f*0.5f*0.333f;//width of one digit = half width of the board for all 3 digits
		digitHeight = digitWidth*2.0f;//predefined ratio
		verticesDigits = new float[]
		{
			   -digitWidth,  -digitHeight, 0f,
				digitWidth,  -digitHeight, 0f,
			   -digitWidth,   digitHeight, 0f,
				digitWidth,  -digitHeight, 0f,
				digitWidth,   digitHeight, 0f,
			   -digitWidth,   digitHeight, 0f
		};
		float textWidth = (GameThread.UPPERLIMITY - GameThread.LOWERLIMITY)*0.5f*0.5f*0.6f;
		float textHeight = textWidth*0.92f;//text ratio
		verticesText = new float[]
		{
				-3.0f*textWidth,  3.0f*digitWidth+textPadding, 0f,
				 3.0f*textWidth,  3.0f*digitWidth+textPadding, 0f,
				-3.0f*textWidth,  textHeight+3.0f*digitWidth+textPadding, 0f,
				 3.0f*textWidth,  3.0f*digitWidth+textPadding, 0f,
				 3.0f*textWidth,  textHeight+3.0f*digitWidth+textPadding, 0f,
				-3.0f*textWidth,  textHeight+3.0f*digitWidth+textPadding, 0f
		};
		verticesBufferDigits = GraphicsUtil.makeFloatBuffer(verticesDigits);
		normalsBuffer = GraphicsUtil.makeFloatBuffer(normals);
		textureCoordsBufferDigits = GraphicsUtil.makeFloatBuffer(textureCoords);
		
		verticesBufferText = GraphicsUtil.makeFloatBuffer(verticesText);
		textureCoordsBuffers[STATE_DISP_WON] = GraphicsUtil.makeFloatBuffer(textureCoordsTextWon);
		textureCoordsBuffers[STATE_DISP_LOST] = GraphicsUtil.makeFloatBuffer(textureCoordsTextLost);
	}
	
	@Override
	public void draw(GL10 gl) {
		//gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);//looks cooler ;)
		//gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glPushMatrix();//save the current matrix, as we will be altering it.
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[4]);//colon
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBufferDigits);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);	
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBufferDigits);
	
		
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		gl.glTranslatef(-digitWidth*2.0f, 0, 0);		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[score.computer]);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		gl.glTranslatef(digitWidth*4.0f, 0, 0);	
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[score.player]);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
		
		gl.glPopMatrix();//restore the matrix, in order to draw the text.

		
		//draw the HUD
		switch (state) {
		default:
		case STATE_DISP_NOTHING:			
			break;
		case STATE_DISP_WON:
		case STATE_DISP_LOST:
			long currTime = System.currentTimeMillis();
			if(currTime-startDispTime > eventDisplayTime) {
				state = STATE_DISP_NOTHING;
			} else {
				//display texture:
				gl.glRotatef(-90, 0, 0, 1f);//rotate so that the width is really the width and not the height
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[TEXTURE_ID_TEXTS]);
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBufferText);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBuffers[state]);
				
				gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);
			}
			break;
		}
		
		
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
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inScaled = false; 
		
		//gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		
		
		gl.glGenTextures(textureIDs.length, textureIDs, 0);
		//load the textures into the graphics memory
		
		//colon
		Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.colon, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[4]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		//digit 0
		bm = BitmapFactory.decodeResource(res, R.drawable.digit0, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 1
		bm = BitmapFactory.decodeResource(res, R.drawable.digit1, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[1]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 2
		bm = BitmapFactory.decodeResource(res, R.drawable.digit2, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[2]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//digit 3
		bm = BitmapFactory.decodeResource(res, R.drawable.digit3, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[3]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		//texts
		bm = BitmapFactory.decodeResource(res, R.drawable.hudtexts, opt);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[TEXTURE_ID_TEXTS]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
	}

	public void playerWon() {
		startDispTime = System.currentTimeMillis();
		state = STATE_DISP_WON;
	}

	public void playerLost() {
		startDispTime = System.currentTimeMillis();
		state = STATE_DISP_LOST;
	}


}
