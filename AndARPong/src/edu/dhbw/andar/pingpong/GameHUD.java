package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import edu.dhbw.andar.pingpong.interfaces.GameEventListener;
import edu.dhbw.andarpong.R;

/**
 * The Head Up Display of the game.
 * Used to display information to the user, like win/loose messages. 
 * @author Tobi
 *
 */
public class GameHUD implements GameEventListener {
	
	//display each message for 2 seconds
	private final static long eventDisplayTime = 2000;
	
	private Resources res;
	
	private int[] textureIDs = new int[2];
	
	private int STATE_DISP_NOTHING=2;
	private int STATE_DISP_WON=1;
	private int STATE_DISP_LOST=0;
	private int state = 0;
	
	private long startDispTime=0;
	
	public GameHUD(Resources res) {
		this.res = res;
	}
	
	/**
	 * Initialize the HUD.
	 * @param gl
	 */
	private void init(GL10 gl) {
		//load resources:
		gl.glGenTextures(2, textureIDs, 0);
		
		Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.youlost);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
		
		bm = BitmapFactory.decodeResource(res, R.drawable.youwon);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[1]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm,0);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		bm.recycle();
	}
	
	/**
	 * draw the current HUD
	 * @param gl
	 */
	private void draw(GL10 gl) {
		/*gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(-100.0f*aspectRatio, 100.0f*aspectRatio, -100.0f, 100.0f, 1.0f, -1.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
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
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[state]);
				//TODO: mode 2d
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
				gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);	
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBuffer);
			}
			break;
		}
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);*/
	}

	@Override
	public void playerWon() {
		startDispTime = System.currentTimeMillis();
		state = STATE_DISP_WON;
	}

	@Override
	public void playerLost() {
		startDispTime = System.currentTimeMillis();
		state = STATE_DISP_LOST;
	}

}
