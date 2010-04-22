package edu.dhbw.andobjviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andobjviewer.ModelViewerActivity.TouchEventHandler;
import edu.dhbw.andobjviewer.graphics.LightingRenderer;
import edu.dhbw.andobjviewer.graphics.Model3D;
import edu.dhbw.andobjviewer.models.Model;
import edu.dhbw.andobjviewer.parser.ObjParser;
import edu.dhbw.andobjviewer.parser.ParseException;
import edu.dhbw.andobjviewer.parser.Util;
import edu.dhbw.andobjviewer.util.FileUtil;

/**
 * Example of an application that makes use of the AndAR toolkit.
 * @author Tobi
 *
 */
public class AugmentedModelViewerActivity extends AndARActivity {
	
	/* Menu Options: */
	private final int MENU_SCALE = 0;
	private final int MENU_ROTATE = 1;
	private final int MENU_TRANSLATE = 2;
	
	private int mode = MENU_TRANSLATE;
	

	private Model model;
	private Model3D model3d;
	private ProgressDialog waitDialog;
	private Resources res;
	
	ARToolkit artoolkit;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setNonARRenderer(new LightingRenderer());//or might be omited
		res=getResources();
		artoolkit = getArtoolkit();
		if(model == null) {
			waitDialog = ProgressDialog.show(this, "", 
	                getResources().getText(R.string.loading), true);
			waitDialog.show();
			new ModelLoader().execute();
		}
		getSurfaceView().setOnTouchListener(new TouchEventHandler());
	}
	
	

	/**
	 * Inform the user about exceptions that occurred in background threads.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("");
	}
	

    /* create the menu
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_TRANSLATE, 0, res.getText(R.string.translate))
    		.setIcon(R.drawable.translate);
        menu.add(0, MENU_ROTATE, 0, res.getText(R.string.rotate))
        	.setIcon(R.drawable.rotate);
        menu.add(0, MENU_SCALE, 0, res.getText(R.string.scale))
        	.setIcon(R.drawable.scale);        
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_SCALE:
	            mode = MENU_SCALE;
	            return true;
	        case MENU_ROTATE:
	        	mode = MENU_ROTATE;
	            return true;
	        case MENU_TRANSLATE:
	        	mode = MENU_TRANSLATE;
	            return true;
        }
        return false;
    }
	
    /**
     * Handles touch events.
     * @author Tobias Domhan
     *
     */
    class TouchEventHandler implements OnTouchListener {
    	
    	private float lastX=0;
    	private float lastY=0;

		/* handles the touch events.
		 * the object will either be scaled, translated or rotated, dependen on the
		 * current user selected mode.
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
		 */
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(model!=null) {
				switch(event.getAction()) {
					//Action started
					default:
					case MotionEvent.ACTION_DOWN:
						lastX = event.getX();
						lastY = event.getY();
						break;
					//Action ongoing
					case MotionEvent.ACTION_MOVE:
						float dX = lastX - event.getX();
						float dY = lastY - event.getY();
						lastX = event.getX();
						lastY = event.getY();
						if(model != null) {
							switch(mode) {
								case MENU_SCALE:
									model.setScale(dY/-1.0f);
						            break;
						        case MENU_ROTATE:
						        	model.setXrot(-1*dY);//dY-> Rotation um die X-Achse
									model.setYrot(-1*dX);//dX-> Rotation um die Y-Achse
						            break;
						        case MENU_TRANSLATE:
						        	model.setXpos(dX/-100f);
									model.setYpos(dY/100f);
						        	break;
							}		
						}
						break;
					//Action ended
					case MotionEvent.ACTION_CANCEL:	
					case MotionEvent.ACTION_UP:
						lastX = event.getX();
						lastY = event.getY();
						break;
				}
			}
			return true;
		}
    	
    }
    
	private class ModelLoader extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected Void doInBackground(Void... params) {
    		//read the model file:
			Resources res = getResources();
			Intent intent = getIntent();
			File modelFile =  new File(URI.create(intent.getDataString()));
			
			FileUtil fileUtil = new FileUtil();
			fileUtil.setBaseFolder(modelFile.getParentFile());
			
			if(modelFile.getAbsolutePath().endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
					if(Config.DEBUG)
						Debug.startMethodTracing("AndObjViewer");
					BufferedReader modelFileReader = new BufferedReader(new FileReader(modelFile));
					String shebang = modelFileReader.readLine();				
					if(!shebang.equals("#trimmed")) {
						//trim the file:			
						File trimmedFile = new File(modelFile.getAbsolutePath()+".tmp");
						BufferedWriter trimmedFileWriter = new BufferedWriter(new FileWriter(trimmedFile));
						Util.trim(modelFileReader, trimmedFileWriter);
						if(modelFile.delete()) {
							trimmedFile.renameTo(modelFile);
							modelFile =  new File(URI.create(intent.getDataString()));
						}					
					}
					model = parser.parse("Model", new BufferedReader(new FileReader(modelFile)));
					if(Config.DEBUG)
						Debug.stopMethodTracing();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			model3d = new Model3D(model);
    		return null;
    	}
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		waitDialog.dismiss();
    		//register model
    		try {
    			if(model!=null)
    				artoolkit.registerARObject(model3d);
			} catch (AndARException e) {
				e.printStackTrace();
			}
    	}
    }
	
	
}
