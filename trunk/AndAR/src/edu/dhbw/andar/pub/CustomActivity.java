package edu.dhbw.andar.pub;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.CameraPreviewHandler;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andopenglcam.R;

/**
 * Example of an application that makes use of the AndAR toolkit.
 * @author Tobi
 *
 */
public class CustomActivity extends AndARActivity {

	CustomObject someObject;
	ARToolkit artoolkit;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//CustomRenderer renderer = new CustomRenderer();//optional, may be set to null
		//super.setNonARRenderer(renderer);//or might be omited
		try {
			artoolkit = super.getArtoolkit();
			someObject = new CustomObject
				("test", "patt.hiro", 80.0, new double[]{0,0});
			artoolkit.registerARObject(someObject);
		} catch (AndARException ex){
			//handle the exception, that means: show the user what happened
			System.out.println("");
		}		
	}

	/**
	 * Inform the user about exceptions that occurred in background threads.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, 0, 0, "reg");
    	menu.add(0, 1, 0, "unreg");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {
			artoolkit.unregisterARObject(someObject);
		} else if(item.getItemId()==0) {
			try {
				someObject = new CustomObject
				("test", "patt.hiro", 80.0, new double[]{0,0});
				artoolkit.registerARObject(someObject);
			} catch (AndARException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	
}
