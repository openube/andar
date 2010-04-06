package edu.dhbw.andar.pub;

import android.os.Bundle;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.interfaces.OpenGLRenderer;

/**
 * Example of an application that makes use of the AndAR toolkit.
 * @author Tobi
 *
 */
public class CustomActivity extends AndARActivity {
	/**
	 * 
	 */
	public CustomActivity() {
		//create a new toolkit object, providing the desired maximun capacity.
		super(25);
		//alternative:
		//super();
		CustomRenderer renderer = new CustomRenderer();//optional, may be set to null
		super.setNonARRenderer(renderer);//or might be omited
		ARToolkit artoolkit = super.getArtoolkit();
		CustomObject someObject = new CustomObject
			("test", "patt.hiro", 80.0, new double[]{0,0});
		artoolkit.registerARObject(someObject);
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
}
