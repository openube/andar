package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * A Paddle controlled by a human being.
 * Either using the touchscreen or a special "paddle maker"
 * @author Tobi
 *
 */
public class PlayerPaddle extends Paddle implements OnTouchListener {
	
	private PaddleMarker marker;

	private float lastTouchX=0;
	private float lastTouchY=0;
	
	private float touchDelta=0;
	private float factor = 0.8f;
	
	
	
	public PlayerPaddle(int ID, PaddleMarker marker, GameCenter center) {
		super(ID, center);
		this.marker = marker;		
	}
	
	@Override
	public synchronized void update(long time) {
		if(marker.isVisible()) {
			oy = y;
			double[] transmat = marker.getTransMatrix();
			double marker_x = transmat[3];
			double marker_y = transmat[7];
			double[] centerTransmat = center.getTransMatrix();
			//transform according to the transformation matrix of the center
			marker_x = center.transform1DX(marker_x);
			marker_y = marker_x-center.getX()-halfWidth;//+halfWidth;
			y = (float)marker_y;
			//the y coordinate is ignored, as the paddle controls only x
			//we use the x coordinate, as the whole board is rotated by 90°
			touchDelta = 0;//reset, while it is controlled by the marker			
		} else {
			//is there any touchevent?
			y += touchDelta;
			touchDelta = 0;
		}
		//Log.d("Paddle_X", y+"");
		checkBoundaries();
	}

	@Override
	public synchronized boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		//Action started
		default:
		case MotionEvent.ACTION_DOWN:
			lastTouchX = event.getX();
			lastTouchY = event.getY();
			break;
		//Action ongoing
		case MotionEvent.ACTION_MOVE:
			float dX = lastTouchX - event.getX();
			float dY = lastTouchY - event.getY();
			lastTouchX = event.getX();
			lastTouchY = event.getY();
			
			touchDelta-=dX;
			touchDelta+=dY;
			break;
		//Action ended
		case MotionEvent.ACTION_CANCEL:	
		case MotionEvent.ACTION_UP:
			lastTouchX = event.getX();
			lastTouchY = event.getY();
			break;
	}
		return true;
	}

	@Override
	public void init(GL10 gl) {
	}

}
