package edu.dhbw.andar.pingpong;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andar.pub.CustomObject;
import edu.dhbw.andar.pub.CustomRenderer;
import edu.dhbw.andarpong.R;

/**
 * Example of an application that makes use of the AndAR toolkit.
 * @author Tobi
 *
 */
public class GameActivity extends AndARActivity {
	
	private final int MENU_SCREENSHOT = 0;
	private GameThread gameThread;
	
	public GameActivity() {
		super(true);
	}

	ARToolkit artoolkit;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		try {
			artoolkit = super.getArtoolkit();
			//Register the marker objects:
			PaddleMarker paddleMarker1 = new PaddleMarker
				("test", "bat.patt", 55.0, new double[]{0,0});//55
			artoolkit.registerARObject(paddleMarker1);
			GameCenter gameCenter = new GameCenter
			("test", "center.patt", 137.0, new double[]{0,0});//170
			artoolkit.registerARObject(gameCenter);
			
			//create all game objects:
			GameScore score = new GameScore();
			GameHUD gameHUD = new GameHUD(this.getResources(),score);
			score.setGameHUD(gameHUD);
			SoundEngine soundEngine = new SoundEngine(this);
			
			Ball ball = new Ball(gameCenter, soundEngine);
			Paddle paddle1 = new PlayerPaddle(0, paddleMarker1, gameCenter);
			getSurfaceView().setOnTouchListener((PlayerPaddle)paddle1);
			//Paddle paddle1 = new ComputerPaddle(0, gameCenter,ball);
			Paddle paddle2 = new ComputerPaddle(1, gameCenter,ball);
			GameBoundary bounderyUp = new GameBoundary(GameThread.UPPERLIMITY);
			GameBoundary bounderyLow = new GameBoundary(GameThread.LOWERLIMITY);
			
			GameRenderer renderer = new GameRenderer(gameCenter);
			super.setNonARRenderer(renderer);
			
			renderer.addGameObject(ball);
			renderer.addGameObject(gameHUD);
			renderer.addGameObject(paddle1);
			renderer.addGameObject(paddle2);
			renderer.addGameObject(bounderyUp);
			renderer.addGameObject(bounderyLow);
			
			
			gameThread = new GameThread(ball, paddle1, paddle2, gameCenter, score);
			
			soundEngine.playBGMusic();
		} catch (AndARException ex){
			//handle the exception, that means: show the user what happened
			Log.e("AndAR Pong", ex.getMessage());
		}	
	}

	/**
	 * Inform the user about exceptions that occurred in background threads.
	 * This exception is rather severe and can not be recovered from.
	 * Inform the user and shut down the application.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("AndAR EXCEPTION", ex.getMessage());
		if(gameThread!= null)
			gameThread.setRunning(false);
		finish();
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(gameThread!= null)
			gameThread.setRunning(false);
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		menu.add(0, MENU_SCREENSHOT, 0, getResources().getText(R.string.takescreenshot))
		.setIcon(R.drawable.screenshoticon);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_SCREENSHOT:
			new TakeAsyncScreenshot().execute();
			break;
		}
		return true;
	}
	
	class TakeAsyncScreenshot extends AsyncTask<Void, Void, Void> {
		
		private String errorMsg = null;

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap bm = takeScreenshot();
			FileOutputStream fos;
			try {
				fos = new FileOutputStream("/sdcard/AndARScreenshot"+new Date().getTime()+".png");
				bm.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();					
			} catch (FileNotFoundException e) {
				errorMsg = e.getMessage();
				e.printStackTrace();
			} catch (IOException e) {
				errorMsg = e.getMessage();
				e.printStackTrace();
			}	
			return null;
		}
		
		protected void onPostExecute(Void result) {
			if(errorMsg == null)
				Toast.makeText(GameActivity.this, getResources().getText(R.string.screenshotsaved), Toast.LENGTH_SHORT ).show();
			else
				Toast.makeText(GameActivity.this, getResources().getText(R.string.screenshotfailed)+errorMsg, Toast.LENGTH_SHORT ).show();
		};
		
	}
	
	
}
